package tn.esprit.eventservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "event_physical_space")
public class EventPhysicalSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnoreProperties("physicalSpaceRefs")
    private Event event;

    @Column(name = "physical_space_id", nullable = false)
    private Long physicalSpaceId;

    public EventPhysicalSpace() {
    }

    public EventPhysicalSpace(Long id, Event event, Long physicalSpaceId) {
        this.id = id;
        this.event = event;
        this.physicalSpaceId = physicalSpaceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Long getPhysicalSpaceId() {
        return physicalSpaceId;
    }

    public void setPhysicalSpaceId(Long physicalSpaceId) {
        this.physicalSpaceId = physicalSpaceId;
    }

    public static EventPhysicalSpaceBuilder builder() {
        return new EventPhysicalSpaceBuilder();
    }

    public static class EventPhysicalSpaceBuilder {
        private Long id;
        private Event event;
        private Long physicalSpaceId;

        public EventPhysicalSpaceBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventPhysicalSpaceBuilder event(Event event) {
            this.event = event;
            return this;
        }

        public EventPhysicalSpaceBuilder physicalSpaceId(Long physicalSpaceId) {
            this.physicalSpaceId = physicalSpaceId;
            return this;
        }

        public EventPhysicalSpace build() {
            return new EventPhysicalSpace(id, event, physicalSpaceId);
        }
    }
}