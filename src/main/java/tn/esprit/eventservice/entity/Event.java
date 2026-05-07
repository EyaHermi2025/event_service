package tn.esprit.eventservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Event")
    @JsonProperty("ID_Event")
    private Long id;

    @Column(name = "title", nullable = false)
    @JsonProperty("Title")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @JsonProperty("Type")
    private EventType type;

    @Column(name = "start_date", nullable = false)
    @JsonProperty("StartDate")
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    @JsonProperty("EndDate")
    private LocalDateTime endDate;

    @Column(name = "manifesto", columnDefinition = "TEXT")
    @JsonProperty("Manifesto")
    private String manifesto;

    @Column(name = "max_participants", nullable = false)
    @JsonProperty("MaxParticipants")
    private Integer maxParticipants;

    @Column(name = "status")
    @JsonProperty("Status")
    private String status;

    @Column(name = "club_id", nullable = false)
    @JsonProperty("ID_Club")
    private Long clubId;

    @Column(name = "estimated_cost")
    @JsonProperty("EstimatedCost")
    @Builder.Default
    private Double estimatedCost = 0.0;

    @Column(name = "difficulty")
    @JsonProperty("Difficulty")
    private String difficulty; // Easy, Medium, Hard

    @Column(name = "teaching_style")
    @JsonProperty("TeachingStyle")
    private String teachingStyle; // Visual, Auditory, Kinesthetic, Mixed

    @Column(name = "efficiency_prediction")
    @JsonProperty("EfficiencyPrediction")
    private Double efficiencyPrediction;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("event")
    @Builder.Default
    @ToString.Exclude
    private List<EventPhysicalSpace> physicalSpaceRefs = new ArrayList<>();
}
