package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.EventPhysicalSpace;

import java.util.List;

@Repository
public interface EventPhysicalSpaceRepository extends JpaRepository<EventPhysicalSpace, Long> {

    List<EventPhysicalSpace> findByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
