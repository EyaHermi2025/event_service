package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventPhysicalSpace;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.dto.BudgetStatsDTO;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventPhysicalSpaceRepository;
import tn.esprit.eventservice.repository.EventRepository;

import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.entity.RegistrationStatus;
import tn.esprit.eventservice.dto.EventRegistrationDto;
import tn.esprit.eventservice.repository.EventRegistrationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import tn.esprit.eventservice.dto.EventStatsDTO;
import tn.esprit.eventservice.exception.BadRequestException;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventPhysicalSpaceRepository eventPhysicalSpaceRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final tn.esprit.eventservice.client.ClubClient clubClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final TicketService ticketService;

    public EventService(EventRepository eventRepository,
            EventPhysicalSpaceRepository eventPhysicalSpaceRepository,
            EventRegistrationRepository eventRegistrationRepository,
            tn.esprit.eventservice.client.ClubClient clubClient,
            SimpMessagingTemplate messagingTemplate,
            TicketService ticketService) {
        this.eventRepository = eventRepository;
        this.eventPhysicalSpaceRepository = eventPhysicalSpaceRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.clubClient = clubClient;
        this.messagingTemplate = messagingTemplate;
        this.ticketService = ticketService;
    }

    public BudgetStatsDTO getBudgetStats() {
        Double totalBudget = eventRepository.findAll().stream()
                .mapToDouble(e -> e.getEstimatedCost() != null ? e.getEstimatedCost() : 0.0)
                .sum();
        long activeEvents = eventRepository.count();
        return new BudgetStatsDTO(totalBudget, activeEvents);
    }

    private void broadcastBudgetUpdate() {
        BudgetStatsDTO stats = getBudgetStats();
        messagingTemplate.convertAndSend("/topic/budget-stats", stats);
    }

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional
    public Event create(Event event, List<Long> physicalSpaceIds) {
        validateEventDatesOnCreate(event);
        if (event.getClubId() != null && event.getEstimatedCost() != null && event.getEstimatedCost() > 0) {
            clubClient.deductBudget(event.getClubId(), event.getEstimatedCost());
        }

        Event saved = eventRepository.save(event);
        if (physicalSpaceIds != null && !physicalSpaceIds.isEmpty()) {
            for (Long psId : physicalSpaceIds) {
                EventPhysicalSpace ref = new EventPhysicalSpace();
                ref.setEvent(saved);
                ref.setPhysicalSpaceId(psId);
                eventPhysicalSpaceRepository.save(ref);
            }
            saved = eventRepository.findById(saved.getId()).orElse(saved);
        }
        broadcastBudgetUpdate();
        return saved;
    }

    @Transactional
    public Event update(Long id, Event details, List<Long> physicalSpaceIds) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));

        validateEventDatesOnUpdate(details, existingEvent);

        existingEvent.setTitle(details.getTitle());
        existingEvent.setType(details.getType());
        existingEvent.setStartDate(details.getStartDate());
        existingEvent.setEndDate(details.getEndDate());
        existingEvent.setManifesto(details.getManifesto());
        existingEvent.setMaxParticipants(details.getMaxParticipants());
        existingEvent.setStatus(details.getStatus());
        existingEvent.setClubId(details.getClubId());

        eventRepository.save(existingEvent);
        eventPhysicalSpaceRepository.deleteByEventId(id);
        if (physicalSpaceIds != null && !physicalSpaceIds.isEmpty()) {
            for (Long psId : physicalSpaceIds) {
                EventPhysicalSpace ref = new EventPhysicalSpace();
                ref.setEvent(existingEvent);
                ref.setPhysicalSpaceId(psId);
                eventPhysicalSpaceRepository.save(ref);
            }
        }
        broadcastBudgetUpdate();
        return eventRepository.findById(id).orElse(existingEvent);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventPhysicalSpaceRepository.deleteByEventId(id);
        eventRepository.deleteById(id);
        broadcastBudgetUpdate();
    }

    @org.springframework.context.event.EventListener
    public void handleWebSocketSubscribeListener(org.springframework.web.socket.messaging.SessionSubscribeEvent event) {
        org.springframework.messaging.simp.stomp.StompHeaderAccessor headerAccessor = org.springframework.messaging.simp.stomp.StompHeaderAccessor
                .wrap(event.getMessage());
        if ("/topic/budget-stats".equals(headerAccessor.getDestination())) {
            // When a client connects, broadcast current stats immediately to them!
            broadcastBudgetUpdate();
        }
    }

    @Transactional(readOnly = true)
    public List<Event> findByClubId(Long clubId) {
        return eventRepository.findByClubId(clubId);
    }

    @Transactional(readOnly = true)
    public List<Event> findByStatus(String status) {
        return eventRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Event> findByType(EventType type) {
        return eventRepository.findByType(type);
    }

    @Transactional
    public EventRegistration registerForEvent(Long eventId, EventRegistrationDto dto) {
        if (dto.getUserId() != null && eventRegistrationRepository.existsByEventIdAndUserId(eventId, dto.getUserId())) {
            throw new BadRequestException("You are already registered for this event.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        RegistrationStatus status;
        
        // Logical check based strictly on the Capacity Limit field value
        if (event.getMaxParticipants() != null && event.getMaxParticipants() > 0) {
            // Success: Stable decrement of the counter
            event.setMaxParticipants(event.getMaxParticipants() - 1);
            status = RegistrationStatus.CONFIRMED;
            eventRepository.save(event); // Persist the new capacity
        } else {
            // Capacity is 0: Send to waitlist
            status = RegistrationStatus.WAITLISTED;
        }

        EventRegistration registration = EventRegistration.builder()
                .eventId(eventId)
                .userName(dto.getUserName())
                .userEmail(dto.getUserEmail())
                .userId(dto.getUserId())
                .registrationDate(LocalDateTime.now())
                .status(status)
                .discoverySource(dto.getDiscoverySource())
                .gender(dto.getGender())
                .reason(dto.getReason())
                .level(dto.getLevel())
                .hobbies(dto.getHobbies())
                .paymentMethod(dto.getPaymentMethod())
                .seatNumber(dto.getSeatNumber())
                .build();

        EventRegistration saved = eventRegistrationRepository.save(registration);

        if (status == RegistrationStatus.CONFIRMED) {
            ticketService.generateAndSendTicket(event, saved);
        }

        broadcastEventStats(eventId);
        return saved;
    }

    @Transactional
    public void cancelRegistration(Long registrationId) {
        EventRegistration reg = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", registrationId));
        
        Long eventId = reg.getEventId();
        RegistrationStatus oldStatus = reg.getStatus();
        
        eventRegistrationRepository.delete(reg);
        
        if (oldStatus == RegistrationStatus.CONFIRMED) {
            List<EventRegistration> waitlist = eventRegistrationRepository
                    .findByEventIdAndStatusOrderByRegistrationDateAsc(eventId, RegistrationStatus.WAITLISTED);
            
            if (!waitlist.isEmpty()) {
                EventRegistration next = waitlist.get(0);
                next.setStatus(RegistrationStatus.CONFIRMED);
                eventRegistrationRepository.save(next);
                
                Event event = eventRepository.findById(eventId).orElse(null);
                if (event != null) {
                    ticketService.generateAndSendTicket(event, next);
                }
            }
        }
        broadcastEventStats(eventId);
    }

    @Transactional
    public void promoteFromWaitlist(Long registrationId) {
        EventRegistration reg = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", registrationId));
        
        if (reg.getStatus() != RegistrationStatus.WAITLISTED) {
            throw new BadRequestException("Registration is not on waitlist.");
        }
        
        reg.setStatus(RegistrationStatus.CONFIRMED);
        eventRegistrationRepository.save(reg);
        
        Event event = eventRepository.findById(reg.getEventId()).orElse(null);
        if (event != null) {
            ticketService.generateAndSendTicket(event, reg);
        }
        broadcastEventStats(reg.getEventId());
    }

    public EventStatsDTO getEventStats(Long eventId) {
        List<EventRegistration> registrations = eventRegistrationRepository.findByEventId(eventId);
        return calculateStats(registrations);
    }

    public EventStatsDTO getGlobalStats() {
        List<EventRegistration> registrations = eventRegistrationRepository.findAll();
        EventStatsDTO stats = calculateStats(registrations);

        Map<Long, Long> countsByEventId = registrations.stream()
                .collect(Collectors.groupingBy(EventRegistration::getEventId, Collectors.counting()));

        List<tn.esprit.eventservice.dto.TopEventDTO> topEvents = countsByEventId.entrySet().stream()
                .sorted(java.util.Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    String title = eventRepository.findById(entry.getKey())
                            .map(Event::getTitle)
                            .orElse("Event #" + entry.getKey());
                    return new tn.esprit.eventservice.dto.TopEventDTO(entry.getKey(), title, entry.getValue());
                })
                .toList();

        stats.setTopEvents(topEvents);
        return stats;
    }

    private EventStatsDTO calculateStats(List<EventRegistration> registrations) {
        if (registrations.isEmpty()) {
            return EventStatsDTO.builder()
                    .discoverySourceDistribution(Map.of())
                    .genderDistribution(Map.of())
                    .specialtyDistribution(Map.of())
                    .paymentMethodDistribution(Map.of())
                    .participationModeDistribution(Map.of())
                    .build();
        }

        long totalInscribed = registrations.size();
        long confirmedCount = registrations.stream().filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED).count();
        long totalAttended = registrations.stream().filter(EventRegistration::getAttended).count();
        double attendanceRate = (double) totalAttended / totalInscribed * 100;
        double averageRating = registrations.stream()
                .filter(r -> r.getFeedbackRating() != null)
                .mapToInt(EventRegistration::getFeedbackRating)
                .average().orElse(0.0);

        Map<String, Long> discovery = registrations.stream()
                .filter(r -> r.getDiscoverySource() != null)
                .collect(Collectors.groupingBy(r -> r.getDiscoverySource().name(), Collectors.counting()));

        Map<String, Long> genders = registrations.stream()
                .filter(r -> r.getGender() != null)
                .collect(Collectors.groupingBy(r -> r.getGender().name(), Collectors.counting()));

        Map<String, Long> specialties = registrations.stream()
                .filter(r -> r.getSpecialty() != null && !r.getSpecialty().isEmpty())
                .collect(Collectors.groupingBy(EventRegistration::getSpecialty, Collectors.counting()));

        Map<String, Long> payments = registrations.stream()
                .filter(r -> r.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(r -> r.getPaymentMethod().name(), Collectors.counting()));

        Map<String, Long> modes = registrations.stream()
                .filter(r -> r.getParticipationMode() != null && !r.getParticipationMode().isEmpty())
                .collect(Collectors.groupingBy(EventRegistration::getParticipationMode, Collectors.counting()));

        return EventStatsDTO.builder()
                .totalInscribed(totalInscribed)
                .confirmedCount(confirmedCount)
                .totalAttended(totalAttended)
                .attendanceRate(attendanceRate)
                .averageRating(averageRating)
                .discoverySourceDistribution(discovery)
                .genderDistribution(genders)
                .specialtyDistribution(specialties)
                .paymentMethodDistribution(payments)
                .participationModeDistribution(modes)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<EventRegistration> getRegistrationById(Long id) {
        return eventRegistrationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean isUserRegistered(Long eventId, Long userId) {
        return eventRegistrationRepository.existsByEventIdAndUserId(eventId, userId);
    }

    @Transactional(readOnly = true)
    public List<EventRegistration> getUserRegistrations(Long userId) {
        return eventRegistrationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<EventRegistration> getRegistrationsByEventAndStatus(Long eventId, RegistrationStatus status) {
        return eventRegistrationRepository.findByEventIdAndStatusOrderByRegistrationDateAsc(eventId, status);
    }

    public void broadcastEventStats(Long eventId) {
        EventStatsDTO stats = getEventStats(eventId);
        messagingTemplate.convertAndSend("/topic/event-stats/" + eventId, stats);

        EventStatsDTO globalStats = getGlobalStats();
        messagingTemplate.convertAndSend("/topic/event-stats/global", globalStats);
    }

    private void validateEventDatesOnCreate(Event event) {
        basicDateCheck(event);
        if (event.getStartDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BadRequestException("Start date must be today or in the future.");
        }
    }

    private void validateEventDatesOnUpdate(Event newDetails, Event existingEvent) {
        basicDateCheck(newDetails);

        if (!newDetails.getStartDate().equals(existingEvent.getStartDate())) {
            if (newDetails.getStartDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
                throw new BadRequestException("New start date must be today or in the future.");
            }
        }
    }

    private void basicDateCheck(Event event) {
        if (event.getStartDate() == null || event.getEndDate() == null) {
            throw new BadRequestException("Start and end dates are required.");
        }
        if (event.getEndDate().isBefore(event.getStartDate())) {
            throw new BadRequestException("End date must be greater than or equal to the start date.");
        }
    }
}