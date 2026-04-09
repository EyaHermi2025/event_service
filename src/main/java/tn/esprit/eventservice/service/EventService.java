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
import tn.esprit.eventservice.dto.EventRegistrationDto;
import tn.esprit.eventservice.repository.EventRegistrationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import tn.esprit.eventservice.dto.EventStatsDTO;
import tn.esprit.eventservice.exception.BadRequestException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventPhysicalSpaceRepository eventPhysicalSpaceRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final tn.esprit.eventservice.client.ClubClient clubClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final TicketService ticketService;

    public EventService(EventRepository eventRepository, EventPhysicalSpaceRepository eventPhysicalSpaceRepository,
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
        // note: estimatedCost update not originally handled here, but if it is updated,
        // we would broadcast anyway

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

    @Transactional(readOnly = true)
    public List<Long> getPhysicalSpaceIdsByEventId(Long eventId) {
        return eventPhysicalSpaceRepository.findByEventId(eventId).stream()
                .map(EventPhysicalSpace::getPhysicalSpaceId)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRegistration registerForEvent(Long eventId, EventRegistrationDto dto) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", eventId);
        }
        EventRegistration registration = new EventRegistration(
                eventId,
                dto.getUserName(),
                dto.getUserEmail(),
                LocalDateTime.now());
        registration.setDiscoverySource(dto.getDiscoverySource());
        registration.setGender(dto.getGender());
        registration.setReason(dto.getReason());
        registration.setLevel(dto.getLevel());
        registration.setHobbies(dto.getHobbies());
        registration.setPaymentMethod(dto.getPaymentMethod());

        EventRegistration saved = eventRegistrationRepository.save(registration);

        // Trigger Ticket Generation and Email (Async)
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event != null) {
            ticketService.generateAndSendTicket(event, saved);
        }

        broadcastEventStats(eventId);
        return saved;
    }

    @Transactional
    public void checkInStudent(String token) {
        // Token format: REG-{id}
        if (!token.startsWith("REG-")) {
            throw new BadRequestException("Invalid ticket format.");
        }

        try {
            Long registrationId = Long.parseLong(token.substring(4));
            EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Registration", registrationId));

            if (registration.isAttended()) {
                throw new BadRequestException("This student is already checked in.");
            }

            registration.setAttended(true);
            eventRegistrationRepository.save(registration);

            // Broadcast update to real-time dashboard
            broadcastEventStats(registration.getEventId());

        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid ticket token.");
        }
    }

    public EventStatsDTO getEventStats(Long eventId) {
        List<EventRegistration> registrations = eventRegistrationRepository.findByEventId(eventId);
        return calculateStats(registrations);
    }

    public EventStatsDTO getGlobalStats() {
        List<EventRegistration> registrations = eventRegistrationRepository.findAll();
        EventStatsDTO stats = calculateStats(registrations);

        // Compute top 5 events by registration count
        Map<Long, Long> countsByEventId = registrations.stream()
                .collect(Collectors.groupingBy(EventRegistration::getEventId, Collectors.counting()));

        List<tn.esprit.eventservice.dto.TopEventDTO> topEvents = countsByEventId.entrySet().stream()
                .sorted(java.util.Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    String title = eventRepository.findById(entry.getKey())
                            .map(e -> e.getTitle())
                            .orElse("Event #" + entry.getKey());
                    return new tn.esprit.eventservice.dto.TopEventDTO(entry.getKey(), title, entry.getValue());
                })
                .collect(Collectors.toList());

        stats.setTopEvents(topEvents);
        return stats;
    }

    private EventStatsDTO calculateStats(List<EventRegistration> registrations) {
        if (registrations.isEmpty()) {
            return new EventStatsDTO(0L, 0L, 0.0, 0.0, Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }

        long totalInscribed = registrations.size();
        long totalAttended = registrations.stream().filter(EventRegistration::isAttended).count();
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

        return new EventStatsDTO(totalInscribed, totalAttended, attendanceRate, averageRating,
                discovery, genders, specialties, payments, modes);
    }

    public void broadcastEventStats(Long eventId) {
        EventStatsDTO stats = getEventStats(eventId);
        messagingTemplate.convertAndSend("/topic/event-stats/" + eventId, stats);

        // Also broadcast global stats
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