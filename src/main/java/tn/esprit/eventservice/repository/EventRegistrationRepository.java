package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.entity.RegistrationStatus;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    List<EventRegistration> findByEventId(Long eventId);
    List<EventRegistration> findByUserEmail(String userEmail);
    List<EventRegistration> findByUserId(Long userId);
    List<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
    List<EventRegistration> findByEventIdAndStatusOrderByRegistrationDateAsc(Long eventId, RegistrationStatus status);
}
