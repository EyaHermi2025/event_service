package tn.esprit.eventservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.entity.EventFeedback;
import tn.esprit.eventservice.repository.EventFeedbackRepository;
import tn.esprit.eventservice.service.FeedbackService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final EventFeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackService feedbackService, EventFeedbackRepository feedbackRepository) {
        this.feedbackService = feedbackService;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * POST /api/events/{id}/feedback
     * Submit a rating (1-5) and an optional comment for a finished event.
     *
     * Example body:
     * {
     * "userEmail": "student@esprit.tn",
     * "rating": 4,
     * "comment": "Très bon événement !"
     * }
     */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(
            @PathVariable("id") Long eventId,
            @RequestBody Map<String, Object> body) {
        try {
            String userEmail = (String) body.get("userEmail");
            Integer rating = (Integer) body.get("rating");
            String comment = (String) body.getOrDefault("comment", "");

            if (userEmail == null || rating == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Les champs 'userEmail' et 'rating' sont obligatoires."));
            }
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La note doit être compris entre 1 et 5."));
            }

            EventFeedback saved = feedbackService.submitFeedback(eventId, userEmail, rating, comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/events/{id}/feedback
     * Retrieve all feedback for a specific event (admin view).
     */
    @GetMapping("/{id}/feedback")
    public ResponseEntity<List<EventFeedback>> getFeedbackForEvent(@PathVariable("id") Long eventId) {
        return ResponseEntity.ok(feedbackRepository.findByEventId(eventId));
    }

    /**
     * GET /api/events/{id}/feedback/score
     * Get the current average satisfaction score for an event.
     */
    @GetMapping("/{id}/feedback/score")
    public ResponseEntity<?> getSatisfactionScore(@PathVariable("id") Long eventId) {
        Double avg = feedbackRepository.calculateAverageRatingByEventId(eventId);
        return ResponseEntity.ok(Map.of(
                "eventId", eventId,
                "averageScore", avg != null ? avg : 0.0,
                "totalFeedbacks", feedbackRepository.findByEventId(eventId).size()));
    }

    /**
     * POST /api/events/{id}/feedback/trigger-for-user
     * Request a feedback email for a specific user email.
     */
    @PostMapping("/{id}/feedback/trigger-for-user")
    public ResponseEntity<?> triggerFeedbackEmailForUser(
            @PathVariable("id") Long eventId,
            @RequestParam("email") String email) {
        try {
            feedbackService.sendFeedbackEmailToUser(eventId, email);
            return ResponseEntity.ok(Map.of("message", "Le lien de feedback a été envoyé à votre adresse email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
