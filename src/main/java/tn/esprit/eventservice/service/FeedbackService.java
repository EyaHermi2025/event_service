package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventFeedback;
import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.repository.EventFeedbackRepository;
import tn.esprit.eventservice.repository.EventRegistrationRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    private final EventFeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EmailService emailService;

    public FeedbackService(EventFeedbackRepository feedbackRepository,
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            EmailService emailService) {
        this.feedbackRepository = feedbackRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.emailService = emailService;
    }

    /**
     * Submit feedback for an event after verifying:
     * - The event exists and has ended.
     * - The same user hasn't already submitted feedback.
     * Then recalculate the average satisfaction score.
     */
    @Transactional
    public EventFeedback submitFeedback(Long eventId, String userEmail, Integer rating, String comment) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (event.getEndDate() != null && event.getEndDate().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Cannot submit feedback for an event that hasn't ended yet.");
        }

        if (feedbackRepository.existsByEventIdAndUserEmail(eventId, userEmail)) {
            throw new RuntimeException("You have already submitted feedback for this event.");
        }

        EventFeedback feedback = new EventFeedback(eventId, userEmail, rating, comment);
        feedbackRepository.save(feedback);

        // Recalculate and update the satisfaction score immediately
        recalculateSatisfactionScore(eventId);

        return feedback;
    }

    /**
     * Recalculates the average satisfaction score for a given event
     * and persists change to the Event entity.
     */
    @Transactional
    public void recalculateSatisfactionScore(Long eventId) {
        Double avg = feedbackRepository.calculateAverageRatingByEventId(eventId);
        if (avg != null) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
            // Round to 2 decimal places
            event.setSatisfactionScore(Math.round(avg * 100.0) / 100.0);
            eventRepository.save(event);
        }
    }

    /**
     * Manually trigger feedback emails for a specific event.
     * Sends a feedback request email to ALL attendees (attended=true).
     */
    @Transactional
    public void sendFeedbackEmailsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (event.getEndDate() != null && event.getEndDate().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Cannot send feedback emails for an event that hasn't ended yet.");
        }

        List<EventRegistration> attendees = registrationRepository
                .findByEventId(event.getId()).stream()
                .filter(EventRegistration::isAttended)
                .toList();

        for (EventRegistration attendee : attendees) {
            sendFeedbackEmail(attendee, event);
        }

        // Mark as sent
        event.setFeedbackSent(true);
        eventRepository.save(event);

        System.out.println("[FeedbackService] Manual feedback emails sent for event: " + event.getTitle()
                + " (" + attendees.size() + " participants)");
    }

    /**
     * Sends a feedback request email to a specific user.
     */
    @Transactional
    public void sendFeedbackEmailToUser(Long eventId, String email) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        EventRegistration registration = registrationRepository.findByEventId(eventId).stream()
                .filter(r -> r.getUserEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Vous n'êtes pas inscrit à cet événement avec cet email (ou vous n'avez pas été marqué comme présent)."));

        if (!registration.isAttended()) {
            throw new RuntimeException("L'envoi du feedback est réservé aux personnes ayant assisté à l'événement.");
        }

        sendFeedbackEmail(registration, event);
        System.out.println(
                "[FeedbackService] Feedback link requested for: " + email + " (Event: " + event.getTitle() + ")");
    }

    private void sendFeedbackEmail(EventRegistration attendee, Event event) {
        String feedbackLink = "http://localhost:4200/feedback?eventId=" + event.getId()
                + "&email=" + attendee.getUserEmail();

        String htmlBody = emailService.generateHtmlFromTemplate("feedback-email",
                Map.of(
                        "userName", attendee.getUserName(),
                        "eventTitle", event.getTitle() != null ? event.getTitle() : "Événement",
                        "feedbackLink", feedbackLink));

        emailService.sendEmailWithAttachment(
                attendee.getUserEmail(),
                "⭐ Donnez votre avis sur \"" + event.getTitle() + "\"",
                htmlBody,
                null,
                null);
    }
}
