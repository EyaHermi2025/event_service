package tn.esprit.eventservice.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "seat")
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
    private Salle salle;

    public Seat() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getxPos() {
        return xPos;
    }

    public void setxPos(Integer xPos) {
        this.xPos = xPos;
    }

    public Integer getyPos() {
        return yPos;
    }

    public void setyPos(Integer yPos) {
        this.yPos = yPos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }
}
