package tn.esprit.eventservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Event")
    @com.fasterxml.jackson.annotation.JsonProperty("ID_Event")
    private Long id;

    @Column(name = "title", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("Title")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("Type")
    private EventType type;

    @Column(name = "start_date", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("StartDate")
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("EndDate")
    private LocalDateTime endDate;

    @Column(name = "manifesto", columnDefinition = "TEXT")
    @com.fasterxml.jackson.annotation.JsonProperty("Manifesto")
    private String manifesto;

    @Column(name = "max_participants", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("MaxParticipants")
    private Integer maxParticipants;

    @Column(name = "status")
    @com.fasterxml.jackson.annotation.JsonProperty("Status")
    private String status;

    @Column(name = "club_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("ID_Club")
    private Long clubId;

    @Column(name = "id_club", nullable = false)
    private Long idClub;

    @Column(name = "estimated_cost")
    @com.fasterxml.jackson.annotation.JsonProperty("EstimatedCost")
    private Double estimatedCost = 0.0;

    @Column(name = "satisfaction_score")
    @com.fasterxml.jackson.annotation.JsonProperty("SatisfactionScore")
    private Double satisfactionScore;

    @Column(name = "feedback_sent", nullable = false)
    private Boolean feedbackSent = false;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("event")
    private List<EventPhysicalSpace> physicalSpaceRefs = new ArrayList<>();

    public Event() {
    }

    public Event(Long id, String title, EventType type, LocalDateTime startDate, LocalDateTime endDate,
            String manifesto, Integer maxParticipants, String status, Long clubId, Double estimatedCost,
            List<EventPhysicalSpace> physicalSpaceRefs) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.manifesto = manifesto;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.clubId = clubId;
        this.idClub = clubId;
        this.estimatedCost = estimatedCost;
        this.physicalSpaceRefs = physicalSpaceRefs != null ? physicalSpaceRefs : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getManifesto() {
        return manifesto;
    }

    public void setManifesto(String manifesto) {
        this.manifesto = manifesto;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
        this.idClub = clubId;
    }

    public List<EventPhysicalSpace> getPhysicalSpaceRefs() {
        return physicalSpaceRefs;
    }

    public void setPhysicalSpaceRefs(List<EventPhysicalSpace> physicalSpaceRefs) {
        this.physicalSpaceRefs = physicalSpaceRefs;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Double getSatisfactionScore() {
        return satisfactionScore;
    }

    public void setSatisfactionScore(Double satisfactionScore) {
        this.satisfactionScore = satisfactionScore;
    }

    public Boolean getFeedbackSent() {
        return feedbackSent;
    }

    public void setFeedbackSent(Boolean feedbackSent) {
        this.feedbackSent = feedbackSent != null ? feedbackSent : false;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static class EventBuilder {
        private Long id;
        private String title;
        private EventType type;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String manifesto;
        private Integer maxParticipants;
        private String status;
        private Long clubId;
        private Double estimatedCost;
        private List<EventPhysicalSpace> physicalSpaceRefs = new ArrayList<>();

        public EventBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EventBuilder type(EventType type) {
            this.type = type;
            return this;
        }

        public EventBuilder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public EventBuilder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public EventBuilder manifesto(String manifesto) {
            this.manifesto = manifesto;
            return this;
        }

        public EventBuilder maxParticipants(Integer maxParticipants) {
            this.maxParticipants = maxParticipants;
            return this;
        }

        public EventBuilder status(String status) {
            this.status = status;
            return this;
        }

        public EventBuilder clubId(Long clubId) {
            this.clubId = clubId;
            return this;
        }

        public EventBuilder estimatedCost(Double estimatedCost) {
            this.estimatedCost = estimatedCost;
            return this;
        }

        public EventBuilder physicalSpaceRefs(List<EventPhysicalSpace> physicalSpaceRefs) {
            this.physicalSpaceRefs = physicalSpaceRefs;
            return this;
        }

        public Event build() {
            return new Event(id, title, type, startDate, endDate, manifesto, maxParticipants, status, clubId,
                    estimatedCost, physicalSpaceRefs);
        }
    }
}
