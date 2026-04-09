package tn.esprit.eventservice.dto;

public class TopEventDTO {
    private Long eventId;
    private String title;
    private long registrationCount;

    public TopEventDTO() {
    }

    public TopEventDTO(Long eventId, String title, long registrationCount) {
        this.eventId = eventId;
        this.title = title;
        this.registrationCount = registrationCount;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getRegistrationCount() {
        return registrationCount;
    }

    public void setRegistrationCount(long registrationCount) {
        this.registrationCount = registrationCount;
    }
}
