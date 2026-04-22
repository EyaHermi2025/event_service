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
class EventServiceTest {

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
        testEvent = Event.builder()
                .id(1L)
                .title("Test Workshop")
                .type(EventType.WORKSHOP)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .estimatedCost(100.0)
                .clubId(10L)
                .build();

        mockDto = EventRegistrationDto.builder()
                .userId(100L)
                .userName("Eya Hermi")
                .userEmail("eya@example.com")
                .build();
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
        verify(clubClient, times(1)).deductBudget(10L, 100.0);
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
        testEvent.setMaxParticipants(5);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        
        EventRegistration savedReg = EventRegistration.builder()
                .id(10L)
                .status(RegistrationStatus.CONFIRMED)
                .build();
        when(eventRegistrationRepository.save(any(EventRegistration.class))).thenReturn(savedReg);

        EventRegistration result = eventService.registerForEvent(1L, mockDto);

        assertEquals(RegistrationStatus.CONFIRMED, result.getStatus());
        assertEquals(4, testEvent.getMaxParticipants());
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void testRegisterForEvent_Waitlisted_WhenCapacityIsZero() {
        testEvent.setMaxParticipants(0);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        
        when(eventRegistrationRepository.save(any(EventRegistration.class))).thenAnswer(invocation -> {
            EventRegistration arg = invocation.getArgument(0);
            arg.setId(11L);
            return arg;
        });

        EventRegistration result = eventService.registerForEvent(1L, mockDto);

        assertEquals(RegistrationStatus.WAITLISTED, result.getStatus());
        assertEquals(0, testEvent.getMaxParticipants());
        verify(eventRepository, never()).save(testEvent);
    }
}
