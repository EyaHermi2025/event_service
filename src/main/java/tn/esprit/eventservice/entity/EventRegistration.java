package tn.esprit.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registration")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
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
    @JsonProperty("status")
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;
}
