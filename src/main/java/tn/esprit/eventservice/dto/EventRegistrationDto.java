package tn.esprit.eventservice.dto;

import tn.esprit.eventservice.entity.DiscoverySource;
import tn.esprit.eventservice.entity.Gender;
import tn.esprit.eventservice.entity.RegistrationReason;
import tn.esprit.eventservice.entity.PaymentMethod;

public class EventRegistrationDto {

    private String userName;
    private String userEmail;
    private DiscoverySource discoverySource;
    private Gender gender;
    private RegistrationReason reason;
    private String level;
    private String hobbies;
    private PaymentMethod paymentMethod;

    public EventRegistrationDto() {
    }

    public EventRegistrationDto(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
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
}
