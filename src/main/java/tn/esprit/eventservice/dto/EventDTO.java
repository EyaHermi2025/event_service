package tn.esprit.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.eventservice.entity.EventType;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
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
}
