package tn.esprit.eventservice.dto;

import java.util.List;
import java.util.Map;

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

    public EventStatsDTO() {
    }

    public EventStatsDTO(long totalInscribed, long confirmedCount, long totalAttended, double attendanceRate, double averageRating,
            Map<String, Long> discoverySourceDistribution, Map<String, Long> genderDistribution,
            Map<String, Long> specialtyDistribution, Map<String, Long> paymentMethodDistribution,
            Map<String, Long> participationModeDistribution) {
        this.totalInscribed = totalInscribed;
        this.confirmedCount = confirmedCount;
        this.totalAttended = totalAttended;
        this.attendanceRate = attendanceRate;
        this.averageRating = averageRating;
        this.discoverySourceDistribution = discoverySourceDistribution;
        this.genderDistribution = genderDistribution;
        this.specialtyDistribution = specialtyDistribution;
        this.paymentMethodDistribution = paymentMethodDistribution;
        this.participationModeDistribution = participationModeDistribution;
    }

    public long getTotalInscribed() {
        return totalInscribed;
    }

    public void setTotalInscribed(long totalInscribed) {
        this.totalInscribed = totalInscribed;
    }

    public long getConfirmedCount() {
        return confirmedCount;
    }

    public void setConfirmedCount(long confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public long getTotalAttended() {
        return totalAttended;
    }

    public void setTotalAttended(long totalAttended) {
        this.totalAttended = totalAttended;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public Map<String, Long> getDiscoverySourceDistribution() {
        return discoverySourceDistribution;
    }

    public void setDiscoverySourceDistribution(Map<String, Long> discoverySourceDistribution) {
        this.discoverySourceDistribution = discoverySourceDistribution;
    }

    public Map<String, Long> getGenderDistribution() {
        return genderDistribution;
    }

    public void setGenderDistribution(Map<String, Long> genderDistribution) {
        this.genderDistribution = genderDistribution;
    }

    public Map<String, Long> getSpecialtyDistribution() {
        return specialtyDistribution;
    }

    public void setSpecialtyDistribution(Map<String, Long> specialtyDistribution) {
        this.specialtyDistribution = specialtyDistribution;
    }

    public Map<String, Long> getPaymentMethodDistribution() {
        return paymentMethodDistribution;
    }

    public void setPaymentMethodDistribution(Map<String, Long> paymentMethodDistribution) {
        this.paymentMethodDistribution = paymentMethodDistribution;
    }

    public Map<String, Long> getParticipationModeDistribution() {
        return participationModeDistribution;
    }

    public void setParticipationModeDistribution(Map<String, Long> participationModeDistribution) {
        this.participationModeDistribution = participationModeDistribution;
    }

    public List<TopEventDTO> getTopEvents() {
        return topEvents;
    }

    public void setTopEvents(List<TopEventDTO> topEvents) {
        this.topEvents = topEvents;
    }
}
