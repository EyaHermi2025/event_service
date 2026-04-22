package tn.esprit.eventservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registration")
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "discovery_source")
    private DiscoverySource discoverySource;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private RegistrationReason reason;

    @Column(name = "level")
    private String level;

    @Column(name = "hobbies")
    private String hobbies;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "attended", nullable = false)
    private Boolean attended = false;

    @Column(name = "feedback_rating")
    private Integer feedbackRating;

    @Column(name = "participation_mode")
    private String participationMode; // ONLINE, PRESENTIAL

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "age")
    private Integer age;

    @Column(name = "seat_number")
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("status")
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    public EventRegistration() {
    }

    public EventRegistration(Long eventId, String userName, String userEmail, Long userId, LocalDateTime registrationDate) {
        this.eventId = eventId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userId = userId;
        this.registrationDate = registrationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public DiscoverySource getDiscoverySource() {
        return discoverySource;
    }

    public void setDiscoverySource(DiscoverySource discoverySource) {
        this.discoverySource = discoverySource;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public RegistrationReason getReason() {
        return reason;
    }

    public void setReason(RegistrationReason reason) {
        this.reason = reason;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isAttended() {
        return attended != null && attended;
    }

    public void setAttended(Boolean attended) {
        this.attended = attended != null ? attended : false;
    }

    public Integer getFeedbackRating() {
        return feedbackRating;
    }

    public void setFeedbackRating(Integer feedbackRating) {
        this.feedbackRating = feedbackRating;
    }

    public String getParticipationMode() {
        return participationMode;
    }

    public void setParticipationMode(String participationMode) {
        this.participationMode = participationMode;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

}
