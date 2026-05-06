package tn.esprit.eventservice.dto;

import lombok.*;
import tn.esprit.eventservice.entity.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationDto {

    private Long id;
    private Long eventId;
    private String userName;
    private String userEmail;
    private Long userId;
    private LocalDateTime registrationDate;
    private DiscoverySource discoverySource;
    private Gender gender;
    private RegistrationReason reason;
    private String level;
    private String hobbies;
    private PaymentMethod paymentMethod;
    private String seatNumber;
    private RegistrationStatus status;
    private Boolean attended;
    private String specialty;
    private String participationMode;
    private String eventTitle;
    private String eventType;
}
