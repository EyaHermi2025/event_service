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

    @GetMapping("/budget-stats")
    public ResponseEntity<tn.esprit.eventservice.dto.BudgetStatsDTO> getBudgetStats() {
        return ResponseEntity.ok(eventService.getBudgetStats());
    }

    @GetMapping("/stats/global")
    public ResponseEntity<EventStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(eventService.getGlobalStats());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Event event,
            @RequestParam(value = "physicalSpaceIds", required = false) List<Long> physicalSpaceIds) {
        try {
            Event created = eventService.create(event, physicalSpaceIds);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (feign.FeignException e) {
            e.printStackTrace();
            java.util.Map<String, String> errorDetails = new java.util.HashMap<>();
            String content = e.contentUTF8();
            String errorMessage = "Insufficient budget or remote service error";

            if (content != null && !content.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(content);
                    if (node.has("message") && !node.get("message").asText().isEmpty()) {
                        errorMessage = node.get("message").asText();
                    } else if (node.has("error") && !node.get("error").asText().isEmpty()) {
                        errorMessage = node.get("error").asText();
                    }
                } catch (Exception ex) {
                    errorMessage = e.getMessage().replaceAll("http://[^/]+", "API");
                }
            }

            errorDetails.put("error", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, String> errorDetails = new java.util.HashMap<>();
            String message = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
            // Remove localhost or IP addresses from the message
            message = message.replaceAll("http://[^/ ]+", "the server");
            errorDetails.put("error", message);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable("id") Long id, @Valid @RequestBody Event event,
            @RequestParam(value = "physicalSpaceIds", required = false) List<Long> physicalSpaceIds) {
        Event updated = eventService.update(id, event, physicalSpaceIds);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        eventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<Event>> getByClubId(@PathVariable("clubId") Long clubId) {
        return ResponseEntity.ok(eventService.findByClubId(clubId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Event>> getByStatus(@PathVariable("status") String status) {
        return ResponseEntity.ok(eventService.findByStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Event>> getByType(@PathVariable("type") EventType type) {
        return ResponseEntity.ok(eventService.findByType(type));
    }

    @GetMapping("/{id}/physical-space-ids")
    public ResponseEntity<List<Long>> getPhysicalSpaceIds(@PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.getPhysicalSpaceIdsByEventId(id));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerForEvent(@PathVariable("id") Long id,
            @Valid @RequestBody EventRegistrationDto dto) {
        try {
            EventRegistration registration = eventService.registerForEvent(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(registration);
        } catch (Exception e) {
            java.util.Map<String, String> errorDetails = new java.util.HashMap<>();
            errorDetails.put("error", e.getMessage() != null ? e.getMessage() : "Failed to register for the event");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
        }
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<EventStatsDTO> getEventStats(@PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.getEventStats(id));
    }

    @PostMapping("/registrations/checkin/{token}")
    public ResponseEntity<?> checkIn(@PathVariable("token") String token) {
        try {
            eventService.checkInStudent(token);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Check-in successful!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}