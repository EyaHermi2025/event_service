package tn.esprit.eventservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.entity.RegistrationStatus;
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.dto.EventRegistrationDto;
import tn.esprit.eventservice.dto.EventStatsDTO;
import tn.esprit.eventservice.dto.BudgetStatsDTO;
import tn.esprit.eventservice.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAll() {
        List<EventDTO> list = eventService.findAll().stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getById(@PathVariable("id") Long id) {
        return eventService.findById(id)
                .map(e -> ResponseEntity.ok(mapToDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventDTO> create(@Valid @RequestBody EventDTO dto) {
        Event created = eventService.create(mapToEntity(dto), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> update(@PathVariable("id") Long id, @Valid @RequestBody EventDTO dto) {
        Event updated = eventService.update(id, mapToEntity(dto), null);
        return ResponseEntity.ok(mapToDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        eventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventRegistrationDto> registerForEvent(@PathVariable("id") Long id,
                                                               @Valid @RequestBody EventRegistrationDto dto) {
        EventRegistration registration = eventService.registerForEvent(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapRegToDTO(registration));
    }

    @GetMapping("/{id}/is-registered")
    public ResponseEntity<Boolean> checkIfRegistered(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(eventService.isUserRegistered(id, userId));
    }

    @GetMapping("/my-registrations")
    public ResponseEntity<List<EventRegistrationDto>> getUserRegistrations(@RequestParam("userId") Long userId) {
        List<EventRegistrationDto> list = eventService.getUserRegistrations(userId).stream()
                .map(this::mapRegToDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<EventStatsDTO> getEventStats(@PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.getEventStats(id));
    }

    @GetMapping("/stats/global")
    public ResponseEntity<EventStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(eventService.getGlobalStats());
    }

    @GetMapping("/budget")
    public ResponseEntity<BudgetStatsDTO> getBudgetStats() {
        return ResponseEntity.ok(eventService.getBudgetStats());
    }

    @GetMapping("/registrations/{id}")
    public ResponseEntity<EventRegistrationDto> getRegistrationById(@PathVariable("id") Long id) {
        return eventService.getRegistrationById(id)
                .map(r -> ResponseEntity.ok(mapRegToDTO(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable("id") Long id) {
        eventService.cancelRegistration(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/waitlist")
    public ResponseEntity<List<EventRegistrationDto>> getWaitlist(@PathVariable("id") Long id) {
        List<EventRegistrationDto> list = eventService.getRegistrationsByEventAndStatus(id, RegistrationStatus.WAITLISTED).stream()
                .map(this::mapRegToDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/registrations/{id}/promote")
    public ResponseEntity<Void> promoteFromWaitlist(@PathVariable("id") Long id) {
        eventService.promoteFromWaitlist(id);
        return ResponseEntity.ok().build();
    }

    private EventDTO mapToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .type(event.getType())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .manifesto(event.getManifesto())
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .clubId(event.getClubId())
                .estimatedCost(event.getEstimatedCost())
                .build();
    }

    private Event mapToEntity(EventDTO dto) {
        return Event.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .type(dto.getType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .manifesto(dto.getManifesto())
                .maxParticipants(dto.getMaxParticipants())
                .status(dto.getStatus())
                .clubId(dto.getClubId())
                .estimatedCost(dto.getEstimatedCost())
                .build();
    }

    private EventRegistrationDto mapRegToDTO(EventRegistration reg) {
        return EventRegistrationDto.builder()
                .id(reg.getId())
                .eventId(reg.getEventId())
                .userName(reg.getUserName())
                .userEmail(reg.getUserEmail())
                .userId(reg.getUserId())
                .registrationDate(reg.getRegistrationDate())
                .discoverySource(reg.getDiscoverySource())
                .gender(reg.getGender())
                .reason(reg.getReason())
                .level(reg.getLevel())
                .hobbies(reg.getHobbies())
                .paymentMethod(reg.getPaymentMethod())
                .seatNumber(reg.getSeatNumber())
                .status(reg.getStatus())
                .attended(reg.getAttended())
                .build();
    }
}