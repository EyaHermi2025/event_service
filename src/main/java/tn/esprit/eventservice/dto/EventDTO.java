package tn.esprit.eventservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("ID_Event")
    private Long id;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Type")
    private EventType type;

    @JsonProperty("StartDate")
    private LocalDateTime startDate;

    @JsonProperty("EndDate")
    private LocalDateTime endDate;

    @JsonProperty("Manifesto")
    private String manifesto;

    @JsonProperty("MaxParticipants")
    private Integer maxParticipants;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("ID_Club")
    private Long clubId;

    @JsonProperty("EstimatedCost")
    private Double estimatedCost;

    @JsonProperty("Difficulty")
    private String difficulty;

    @JsonProperty("TeachingStyle")
    private String teachingStyle;

    @JsonProperty("EfficiencyPrediction")
    private Double efficiencyPrediction;
}
