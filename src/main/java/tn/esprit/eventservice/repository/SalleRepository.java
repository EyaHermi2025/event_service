package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.Salle;

@Repository
public interface SalleRepository extends JpaRepository<Salle, Long> {
}
