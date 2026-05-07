package tn.esprit.eventservice;

import org.junit.jupiter.api.Test;
import tn.esprit.eventservice.dto.*;
import tn.esprit.eventservice.entity.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class specifically designed to cover boilerplate code (getters, setters, builders)
 * for Entities and DTOs to satisfy SonarCloud coverage requirements.
 */
class BoilerplateCoverageTest {

    @Test
    void testEventEntity() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .id(1L)
                .title("Test")
                .type(EventType.WORKSHOP)
                .startDate(now)
                .endDate(now.plusHours(2))
                .manifesto("Manifesto")
                .maxParticipants(100)
                .status("ACTIVE")
                .clubId(10L)
                .estimatedCost(500.0)
                .difficulty("Medium")
                .teachingStyle("Visual")
                .efficiencyPrediction(0.75)
                .physicalSpaceRefs(new java.util.ArrayList<>())
                .build();

        assertEquals(1L, event.getId());
        assertEquals("Test", event.getTitle());
        assertEquals(EventType.WORKSHOP, event.getType());
        assertEquals(now, event.getStartDate());
        assertEquals(now.plusHours(2), event.getEndDate());
        assertEquals("Manifesto", event.getManifesto());
        assertEquals(100, event.getMaxParticipants());
        assertEquals("ACTIVE", event.getStatus());
        assertEquals(10L, event.getClubId());
        assertEquals(500.0, event.getEstimatedCost());
        assertEquals("Medium", event.getDifficulty());
        assertEquals("Visual", event.getTeachingStyle());
        assertEquals(0.75, event.getEfficiencyPrediction());
        assertNotNull(event.getPhysicalSpaceRefs());

        event.setTitle("New Title");
        assertEquals("New Title", event.getTitle());

        assertNotNull(event.toString());
        assertNotEquals(0, event.hashCode());
        
        Event event2 = Event.builder().id(1L).build();
        Event event3 = Event.builder().id(1L).build();
        assertEquals(event2, event3);
    }

    @Test
    void testEventRegistrationEntity() {
        LocalDateTime now = LocalDateTime.now();
        EventRegistration reg = EventRegistration.builder()
                .id(1L)
                .eventId(2L)
                .userName("User")
                .userEmail("user@test.com")
                .userId(100L)
                .registrationDate(now)
                .discoverySource(DiscoverySource.FACEBOOK)
                .gender(Gender.FEMALE)
                .reason(RegistrationReason.LEARNING)
                .level("Intermediate")
                .hobbies("Coding")
                .paymentMethod(PaymentMethod.CARD)
                .attended(true)
                .feedbackRating(5)
                .participationMode("ONLINE")
                .specialty("IT")
                .age(25)
                .seatNumber("A1")
                .status(RegistrationStatus.CONFIRMED)
                .build();

        assertEquals(1L, reg.getId());
        assertEquals(2L, reg.getEventId());
        assertEquals("User", reg.getUserName());
        assertEquals("user@test.com", reg.getUserEmail());
        assertEquals(100L, reg.getUserId());
        assertEquals(now, reg.getRegistrationDate());
        assertEquals(DiscoverySource.FACEBOOK, reg.getDiscoverySource());
        assertEquals(Gender.FEMALE, reg.getGender());
        assertEquals(RegistrationReason.LEARNING, reg.getReason());
        assertEquals("Intermediate", reg.getLevel());
        assertEquals("Coding", reg.getHobbies());
        assertEquals(PaymentMethod.CARD, reg.getPaymentMethod());
        assertTrue(reg.getAttended());
        assertEquals(5, reg.getFeedbackRating());
        assertEquals("ONLINE", reg.getParticipationMode());
        assertEquals("IT", reg.getSpecialty());
        assertEquals(25, reg.getAge());
        assertEquals("A1", reg.getSeatNumber());
        assertEquals(RegistrationStatus.CONFIRMED, reg.getStatus());

        assertNotNull(reg.toString());
    }

    @Test
    void testEventDTO() {
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("DTO")
                .type(EventType.MEETING)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(1))
                .manifesto("M")
                .maxParticipants(10)
                .status("S")
                .clubId(1L)
                .estimatedCost(10.0)
                .difficulty("E")
                .teachingStyle("V")
                .efficiencyPrediction(0.5)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("DTO", dto.getTitle());
        assertNotNull(dto.toString());
        
        EventDTO dto2 = new EventDTO();
        dto2.setId(1L);
        assertEquals(1L, dto2.getId());
    }

    @Test
    void testEventRegistrationDto() {
        EventRegistrationDto dto = EventRegistrationDto.builder()
                .id(1L)
                .eventId(2L)
                .userName("Eya")
                .userEmail("eya@test.com")
                .userId(100L)
                .registrationDate(LocalDateTime.now())
                .discoverySource(DiscoverySource.CLUB)
                .gender(Gender.OTHER)
                .reason(RegistrationReason.FUN)
                .level("Beginner")
                .hobbies("Music")
                .paymentMethod(PaymentMethod.CASH)
                .seatNumber("B1")
                .status(RegistrationStatus.WAITLISTED)
                .attended(false)
                .specialty("Math")
                .participationMode("PRESENTIAL")
                .eventTitle("Title")
                .eventType("WORKSHOP")
                .build();

        assertEquals("Eya", dto.getUserName());
        assertEquals("Title", dto.getEventTitle());
        assertNotNull(dto.toString());
        
        EventRegistrationDto dto2 = new EventRegistrationDto();
        dto2.setUserName("New");
        assertEquals("New", dto2.getUserName());
    }

    @Test
    void testEventStatsDTO() {
        EventStatsDTO stats = EventStatsDTO.builder()
                .totalInscribed(10)
                .confirmedCount(8)
                .totalAttended(5)
                .attendanceRate(50.0)
                .averageRating(4.5)
                .discoverySourceDistribution(Map.of("FB", 5L))
                .genderDistribution(Map.of("F", 5L))
                .specialtyDistribution(Map.of("IT", 5L))
                .paymentMethodDistribution(Map.of("CARD", 5L))
                .participationModeDistribution(Map.of("ONLINE", 5L))
                .topEvents(Collections.emptyList())
                .build();

        assertEquals(10, stats.getTotalInscribed());
        assertNotNull(stats.getDiscoverySourceDistribution());
        assertNotNull(stats.toString());
    }

    @Test
    void testBudgetStatsDTO() {
        BudgetStatsDTO stats = BudgetStatsDTO.builder()
                .totalEstimatedCost(1000.0)
                .activeEventsCount(5L)
                .build();
        assertEquals(1000.0, stats.getTotalEstimatedCost());
        assertEquals(5L, stats.getActiveEventsCount());
        
        BudgetStatsDTO stats2 = new BudgetStatsDTO();
        stats2.setActiveEventsCount(1L);
        assertEquals(1L, stats2.getActiveEventsCount());
    }

    @Test
    void testTopEventDTO() {
        TopEventDTO dto = new TopEventDTO(1L, "Title", 10L);
        assertEquals(1L, dto.getEventId());
        assertEquals("Title", dto.getTitle());
        assertEquals(10L, dto.getRegistrationCount());

        dto.setEventId(2L);
        dto.setTitle("New");
        dto.setRegistrationCount(5L);
        assertEquals(2L, dto.getEventId());
        assertEquals("New", dto.getTitle());
        assertEquals(5L, dto.getRegistrationCount());
        
        TopEventDTO dto2 = new TopEventDTO();
        assertNotNull(dto2);
    }
    
    @Test
    void testPhysicalSpaceDTO() {
        PhysicalSpaceDTO dto = PhysicalSpaceDTO.builder()
                .id(1L)
                .name("Space")
                .capacity(10)
                .build();
        assertEquals("Space", dto.getName());
        
        PhysicalSpaceDTO dto2 = new PhysicalSpaceDTO();
        dto2.setName("N");
        assertEquals("N", dto2.getName());
    }

    @Test
    void testSalleAndSeat() {
        Salle salle = Salle.builder().id(1L).name("Salle 1").capacity(20).build();
        Seat seat = Seat.builder().id(1L).rowLabel("A").seatNumber(1).salle(salle).build();
        salle.setSeats(Collections.singletonList(seat));

        assertEquals("Salle 1", salle.getName());
        assertEquals(salle, seat.getSalle());
        assertNotNull(salle.toString());
        assertNotNull(seat.toString());
        
        Salle salle2 = new Salle();
        salle2.setName("S2");
        assertEquals("S2", salle2.getName());
        
        Seat seat2 = new Seat();
        seat2.setRowLabel("B");
        assertEquals("B", seat2.getRowLabel());
    }
    
    @Test
    void testEventPhysicalSpace() {
        Event event = Event.builder().id(1L).build();
        EventPhysicalSpace eps = EventPhysicalSpace.builder()
                .id(1L)
                .event(event)
                .physicalSpaceId(10L)
                .build();
        
        assertEquals(1L, eps.getId());
        assertEquals(event, eps.getEvent());
        assertEquals(10L, eps.getPhysicalSpaceId());
        
        eps.setPhysicalSpaceId(20L);
        assertEquals(20L, eps.getPhysicalSpaceId());
        
        assertNotNull(eps.toString());
        
        EventPhysicalSpace eps2 = new EventPhysicalSpace();
        eps2.setId(2L);
        assertEquals(2L, eps2.getId());
    }
}
