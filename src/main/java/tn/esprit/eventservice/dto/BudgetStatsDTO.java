package tn.esprit.eventservice.dto;

public class BudgetStatsDTO {
    private Double totalEstimatedCost;
    private Long activeEventsCount;

    public BudgetStatsDTO(Double totalEstimatedCost, Long activeEventsCount) {
        this.totalEstimatedCost = totalEstimatedCost;
        this.activeEventsCount = activeEventsCount;
    }

    public Double getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(Double totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public Long getActiveEventsCount() {
        return activeEventsCount;
    }

    public void setActiveEventsCount(Long activeEventsCount) {
        this.activeEventsCount = activeEventsCount;
    }
}
