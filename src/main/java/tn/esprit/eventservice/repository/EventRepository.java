package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventType;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByClubId(Long clubId);

    List<Event> findByStatus(String status);

    List<Event> findByType(EventType type);
}
