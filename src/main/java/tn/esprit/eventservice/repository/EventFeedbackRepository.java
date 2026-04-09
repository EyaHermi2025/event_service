package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.EventFeedback;

import java.util.List;

@Repository
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Long> {

    List<EventFeedback> findByEventId(Long eventId);

    @Query("SELECT AVG(f.rating) FROM EventFeedback f WHERE f.eventId = :eventId")
    Double calculateAverageRatingByEventId(@Param("eventId") Long eventId);

    boolean existsByEventIdAndUserEmail(Long eventId, String userEmail);
}
