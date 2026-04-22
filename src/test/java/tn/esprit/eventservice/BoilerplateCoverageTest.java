package tn.esprit.eventservice;

import org.junit.jupiter.api.Test;
import tn.esprit.eventservice.dto.*;
import tn.esprit.eventservice.entity.*;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class specifically designed to cover boilerplate code (getters, setters, builders)
 * for Entities and DTOs to satisfy SonarCloud coverage requirements.
 */
class BoilerplateCoverageTest {

    @Test
    void testEntitiesBoilerplate() {
        // Event
        Event event = Event.builder()
                .id(1L)
                .title("Test")
                .type(EventType.WORKSHOP)
                .startDate(LocalDateTime.now())
                .status("ACTIVE")
                .build();
        assertEquals(1L, event.getId());
        assertEquals("Test", event.getTitle());
        assertNotNull(event.toString());
        assertNotEquals(0, event.hashCode());
        Event sameEvent = Event.builder().id(1L).title("Test").build();
        assertEquals(event, sameEvent);
        assertNotEquals(null, event);

        // EventRegistration
        EventRegistration reg = EventRegistration.builder()
                .id(1L)
                .userName("User")
                .status(RegistrationStatus.CONFIRMED)
                .build();
        assertEquals(1L, reg.getId());
        assertEquals("User", reg.getUserName());
        assertNotNull(reg.toString());

        // Salle & Seat
        Salle salle = Salle.builder().id(1L).name("Room 101").capacity(50).build();
        Seat seat = Seat.builder().id(1L).rowLabel("A").seatNumber(1).salle(salle).build();
        salle.setSeats(Collections.singletonList(seat));
        
        assertEquals("Room 101", salle.getName());
        assertEquals("A", seat.getRowLabel());
        assertNotNull(salle.toString());
        assertNotNull(seat.toString());
    }

    @Test
    void testDtoBoilerplate() {
        // EventDTO
        EventDTO eventDto = EventDTO.builder().id(1L).title("DTO").build();
        EventDTO eventDto2 = EventDTO.builder().id(1L).title("DTO").build();
        assertEquals(eventDto, eventDto2);
        assertEquals(eventDto.hashCode(), eventDto2.hashCode());
        assertEquals(1L, eventDto.getId());
        assertEquals("DTO", eventDto.getTitle());

        // EventRegistrationDto
        EventRegistrationDto regDto = EventRegistrationDto.builder().userName("Eya").build();
        assertEquals("Eya", regDto.getUserName());

        // EventStatsDTO
        EventStatsDTO statsDto = EventStatsDTO.builder().totalInscribed(10).build();
        assertEquals(10, statsDto.getTotalInscribed());

        // PhysicalSpaceDTO
        PhysicalSpaceDTO psDto = PhysicalSpaceDTO.builder().name("Space").build();
        assertEquals("Space", psDto.getName());
    }
}
