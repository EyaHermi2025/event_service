package tn.esprit.eventservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.entity.RegistrationStatus;
import tn.esprit.eventservice.dto.EventRegistrationDto;
import tn.esprit.eventservice.repository.EventPhysicalSpaceRepository;
import tn.esprit.eventservice.repository.EventRegistrationRepository;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.client.ClubClient;
import tn.esprit.eventservice.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPhysicalSpaceRepository eventPhysicalSpaceRepository;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private ClubClient clubClient;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private EventRegistrationDto mockDto;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Workshop");
        testEvent.setType(EventType.WORKSHOP);
        testEvent.setStartDate(LocalDateTime.now().plusDays(1));
        testEvent.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2));
        testEvent.setEstimatedCost(100.0);
        testEvent.setClubId(10L);

        mockDto = new EventRegistrationDto();
        mockDto.setUserId(100L);
        mockDto.setUserName("Eya Hermi");
        mockDto.setUserEmail("eya@example.com");
    }

    @Test
    void testFindById_Found() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        Optional<Event> found = eventService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Test Workshop", found.get().getTitle());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Event> found = eventService.findById(2L);

        assertFalse(found.isPresent());
    }

    @Test
    void testCreate_Success() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event created = eventService.create(testEvent, null);

        assertNotNull(created);
        assertEquals("Test Workshop", created.getTitle());
        verify(clubClient, times(1)).deductBudget(eq(10L), eq(100.0));
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void testDeleteById_Success() {
        when(eventRepository.existsById(1L)).thenReturn(true);

        eventService.deleteById(1L);

        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.deleteById(1L);
        });
    }

    @Test
    void testRegisterForEvent_Success_WhenCapacityAvailable() {
        testEvent.setMaxParticipants(5); // Capacity strictly > 0
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        
        EventRegistration savedReg = new EventRegistration();
        savedReg.setId(10L);
        savedReg.setStatus(RegistrationStatus.CONFIRMED);
        when(eventRegistrationRepository.save(any(EventRegistration.class))).thenReturn(savedReg);

        EventRegistration result = eventService.registerForEvent(1L, mockDto);

        assertEquals(RegistrationStatus.CONFIRMED, result.getStatus());
        assertEquals(4, testEvent.getMaxParticipants()); // Decrement triggered
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void testRegisterForEvent_Waitlisted_WhenCapacityIsZero() {
        testEvent.setMaxParticipants(0); // Capacity == 0
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        
        EventRegistration savedReg = new EventRegistration();
        savedReg.setId(11L);
        savedReg.setStatus(RegistrationStatus.WAITLISTED);
        when(eventRegistrationRepository.save(any(EventRegistration.class))).thenAnswer(invocation -> {
            EventRegistration arg = invocation.getArgument(0);
            arg.setId(11L);
            return arg;
        });

        EventRegistration result = eventService.registerForEvent(1L, mockDto);

        assertEquals(RegistrationStatus.WAITLISTED, result.getStatus());
        assertEquals(0, testEvent.getMaxParticipants()); // No decrement if waitlisted
        verify(eventRepository, never()).save(testEvent); // Capacity not updated in DB
    }
}
