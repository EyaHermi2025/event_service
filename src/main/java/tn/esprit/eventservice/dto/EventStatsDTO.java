package tn.esprit.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatsDTO {
    private long totalInscribed;
    private long confirmedCount;
    private long totalAttended;
    private double attendanceRate;
    private double averageRating;

    // Distribution charts data
    private Map<String, Long> discoverySourceDistribution;
    private Map<String, Long> genderDistribution;
    private Map<String, Long> specialtyDistribution;
    private Map<String, Long> paymentMethodDistribution;
    private Map<String, Long> participationModeDistribution;

    // Top events by registration count (for global stats)
    private List<TopEventDTO> topEvents;
}
