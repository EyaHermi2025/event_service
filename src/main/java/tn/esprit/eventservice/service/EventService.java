package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventPhysicalSpace;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventPhysicalSpaceRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventPhysicalSpaceRepository eventPhysicalSpaceRepository;
    private final tn.esprit.eventservice.client.ClubClient clubClient;

    public EventService(EventRepository eventRepository, EventPhysicalSpaceRepository eventPhysicalSpaceRepository,
            tn.esprit.eventservice.client.ClubClient clubClient) {
        this.eventRepository = eventRepository;
        this.eventPhysicalSpaceRepository = eventPhysicalSpaceRepository;
        this.clubClient = clubClient;
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
        // Enforce budget constraint
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
        return saved;
    }

    @Transactional
    public Event update(Long id, Event details, List<Long> physicalSpaceIds) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        event.setTitle(details.getTitle());
        event.setType(details.getType());
        event.setStartDate(details.getStartDate());
        event.setEndDate(details.getEndDate());
        event.setManifesto(details.getManifesto());
        event.setMaxParticipants(details.getMaxParticipants());
        event.setStatus(details.getStatus());
        event.setClubId(details.getClubId());
        eventRepository.save(event);
        eventPhysicalSpaceRepository.deleteByEventId(id);
        if (physicalSpaceIds != null && !physicalSpaceIds.isEmpty()) {
            for (Long psId : physicalSpaceIds) {
                EventPhysicalSpace ref = new EventPhysicalSpace();
                ref.setEvent(event);
                ref.setPhysicalSpaceId(psId);
                eventPhysicalSpaceRepository.save(ref);
            }
        }
        return eventRepository.findById(id).orElse(event);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventPhysicalSpaceRepository.deleteByEventId(id);
        eventRepository.deleteById(id);
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
}