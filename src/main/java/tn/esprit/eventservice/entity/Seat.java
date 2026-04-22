package tn.esprit.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "seat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String rowLabel;
    private Integer seatNumber;
    private Integer xPos;
    private Integer yPos;
    private String type; // STANDARD, VIP, PMR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id")
    @JsonBackReference
    @ToString.Exclude
    private Salle salle;
}
