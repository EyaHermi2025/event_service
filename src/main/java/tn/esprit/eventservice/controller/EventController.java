package tn.esprit.eventservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.dto.EventRegistrationDto;
import tn.esprit.eventservice.dto.EventStatsDTO;
import tn.esprit.eventservice.service.EventService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAll() {
        return ResponseEntity.ok(eventService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getById(@PathVariable("id") Long id) {
        return eventService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Event event) {
        try {
            Event created = eventService.create(event, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            java.util.Map<String, String> errorDetails = new java.util.HashMap<>();
            errorDetails.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable("id") Long id, @Valid @RequestBody Event event) {
        Event updated = eventService.update(id, event, null);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        eventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerForEvent(@PathVariable("id") Long id,
            @Valid @RequestBody EventRegistrationDto dto) {
        try {
            EventRegistration registration = eventService.registerForEvent(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(registration);
        } catch (Exception e) {
            java.util.Map<String, String> errorDetails = new java.util.HashMap<>();
            errorDetails.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
        }
    }

    @GetMapping("/{id}/is-registered")
    public ResponseEntity<Boolean> checkIfRegistered(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(eventService.isUserRegistered(id, userId));
    }

    @GetMapping("/my-registrations")
    public ResponseEntity<List<EventRegistration>> getUserRegistrations(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(eventService.getUserRegistrations(userId));
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
    public ResponseEntity<tn.esprit.eventservice.dto.BudgetStatsDTO> getBudgetStats() {
        return ResponseEntity.ok(eventService.getBudgetStats());
    }

    @GetMapping("/registrations/{id}")
    public ResponseEntity<EventRegistration> getRegistrationById(@PathVariable("id") Long id) {
        return eventService.getRegistrationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable("id") Long id) {
        eventService.cancelRegistration(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/waitlist")
    public ResponseEntity<List<EventRegistration>> getWaitlist(@PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.getRegistrationsByEventAndStatus(id, tn.esprit.eventservice.entity.RegistrationStatus.WAITLISTED));
    }

    @PostMapping("/registrations/{id}/promote")
    public ResponseEntity<Void> promoteFromWaitlist(@PathVariable("id") Long id) {
        eventService.promoteFromWaitlist(id);
        return ResponseEntity.ok().build();
    }
}