package tn.esprit.eventservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import tn.esprit.eventservice.entity.*;
import tn.esprit.eventservice.dto.*;
import tn.esprit.eventservice.repository.EventPhysicalSpaceRepository;
import tn.esprit.eventservice.repository.EventRegistrationRepository;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.client.ClubClient;
import tn.esprit.eventservice.client.MLPredictionClient;
import tn.esprit.eventservice.exception.BadRequestException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private EventPhysicalSpaceRepository eventPhysicalSpaceRepository;
    @Mock private EventRegistrationRepository eventRegistrationRepository;
    @Mock private ClubClient clubClient;
    @Mock private MLPredictionClient mlPredictionClient;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private TicketService ticketService;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private EventRegistrationDto mockDto;

    @BeforeEach
    void setUp() {
        testEvent = Event.builder()
                .id(1L).title("Test Workshop").type(EventType.WORKSHOP)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .estimatedCost(100.0).clubId(10L).maxParticipants(5)
                .build();
        mockDto = EventRegistrationDto.builder()
                .userId(100L).userName("Eya Hermi").userEmail("eya@example.com")
                .build();
    }

    // ── findAll ──
    @Test
    void testFindAll() {
        when(eventRepository.findAll()).thenReturn(List.of(testEvent));
        List<Event> result = eventService.findAll();
        assertEquals(1, result.size());
        assertEquals("Test Workshop", result.get(0).getTitle());
    }

    // ── findById ──
    @Test
    void testFindById_Found() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        assertTrue(eventService.findById(1L).isPresent());
    }

    @Test
    void testFindById_NotFound() {
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(eventService.findById(2L).isPresent());
    }

    // ── create ──
    @Test
    void testCreate_Success() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        Event created = eventService.create(testEvent, null);
        assertNotNull(created);
        verify(clubClient).deductBudget(10L, 100.0);
    }

    @Test
    void testCreate_WithPhysicalSpaces() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        Event created = eventService.create(testEvent, List.of(1L, 2L));
        assertNotNull(created);
        verify(eventPhysicalSpaceRepository, times(2)).save(any(EventPhysicalSpace.class));
    }

    @Test
    void testCreate_NoClubId() {
        testEvent.setClubId(null);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        eventService.create(testEvent, null);
        verify(clubClient, never()).deductBudget(anyLong(), anyDouble());
    }

    @Test
    void testCreate_ZeroCost() {
        testEvent.setEstimatedCost(0.0);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        eventService.create(testEvent, null);
        verify(clubClient, never()).deductBudget(anyLong(), anyDouble());
    }

    @Test
    void testCreate_NullCost() {
        testEvent.setEstimatedCost(null);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        eventService.create(testEvent, null);
        verify(clubClient, never()).deductBudget(anyLong(), anyDouble());
    }

    @Test
    void testCreate_InvalidDates_EndBeforeStart() {
        testEvent.setEndDate(testEvent.getStartDate().minusHours(1));
        assertThrows(BadRequestException.class, () -> eventService.create(testEvent, null));
    }

    @Test
    void testCreate_NullStartDate() {
        testEvent.setStartDate(null);
        assertThrows(BadRequestException.class, () -> eventService.create(testEvent, null));
    }

    @Test
    void testCreate_NullEndDate() {
        testEvent.setEndDate(null);
        assertThrows(BadRequestException.class, () -> eventService.create(testEvent, null));
    }

    @Test
    void testCreate_StartDateInPast() {
        testEvent.setStartDate(LocalDateTime.now().minusDays(2));
        testEvent.setEndDate(LocalDateTime.now().minusDays(1));
        assertThrows(BadRequestException.class, () -> eventService.create(testEvent, null));
    }

    @Test
    void testCreate_WithMLPrediction() {
        testEvent.setDifficulty("Medium");
        testEvent.setTeachingStyle("Visual");
        testEvent.setType(EventType.COMPETITION);
        MLPredictionClient.PredictionResponse resp = new MLPredictionClient.PredictionResponse();
        resp.setEfficiency_probability(0.85);
        when(mlPredictionClient.predict(any())).thenReturn(resp);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        eventService.create(testEvent, null);
        assertEquals(0.85, testEvent.getEfficiencyPrediction());
    }

    @Test
    void testCreate_MLPredictionFails() {
        testEvent.setDifficulty("Hard");
        testEvent.setTeachingStyle("Auditory");
        testEvent.setType(EventType.MEETING);
        when(mlPredictionClient.predict(any())).thenThrow(new RuntimeException("ML down"));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        assertDoesNotThrow(() -> eventService.create(testEvent, null));
    }

    @Test
    void testCreate_MLPredictionNullResponse() {
        testEvent.setDifficulty("Easy");
        testEvent.setTeachingStyle("Mixed");
        testEvent.setType(EventType.WORKSHOP);
        when(mlPredictionClient.predict(any())).thenReturn(null);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        eventService.create(testEvent, null);
        assertNull(testEvent.getEfficiencyPrediction());
    }

    @Test
    void testCreate_MLPredictionSkippedWhenFieldsNull() {
        testEvent.setDifficulty(null);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        eventService.create(testEvent, null);
        verify(mlPredictionClient, never()).predict(any());
    }

    // ── update ──
    @Test
    void testUpdate_Success() {
        Event details = Event.builder().title("Updated").type(EventType.MEETING)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        Event result = eventService.update(1L, details, null);
        assertNotNull(result);
        verify(eventPhysicalSpaceRepository).deleteByEventId(1L);
    }

    @Test
    void testUpdate_WithPhysicalSpaces() {
        Event details = Event.builder().title("Updated").type(EventType.MEETING)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, List.of(3L));
        verify(eventPhysicalSpaceRepository).save(any(EventPhysicalSpace.class));
    }

    @Test
    void testUpdate_NotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        Event details = Event.builder().build();
        assertThrows(ResourceNotFoundException.class, () -> eventService.update(99L, details, null));
    }

    @Test
    void testUpdate_ChangedStartDateInPast() {
        Event details = Event.builder().title("X").type(EventType.MEETING)
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(1)).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        assertThrows(BadRequestException.class, () -> eventService.update(1L, details, null));
    }

    @Test
    void testUpdate_SameStartDateNotRejected() {
        Event details = Event.builder().title("X").type(EventType.MEETING)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate()).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        assertDoesNotThrow(() -> eventService.update(1L, details, null));
    }

    @Test
    void testUpdate_WithMLPrediction_Workshop() {
        Event details = Event.builder().title("Updated").type(EventType.WORKSHOP)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Easy").teachingStyle("Visual").build();
        MLPredictionClient.PredictionResponse resp = new MLPredictionClient.PredictionResponse();
        resp.setEfficiency_probability(0.72);
        when(mlPredictionClient.predict(any())).thenReturn(resp);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        assertEquals(0.72, testEvent.getEfficiencyPrediction());
    }

    @Test
    void testUpdate_WithMLPrediction_Meeting() {
        Event details = Event.builder().title("Updated").type(EventType.MEETING)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Hard").teachingStyle("Auditory").build();
        MLPredictionClient.PredictionResponse resp = new MLPredictionClient.PredictionResponse();
        resp.setEfficiency_probability(0.60);
        when(mlPredictionClient.predict(any())).thenReturn(resp);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        assertEquals(0.60, testEvent.getEfficiencyPrediction());
    }

    @Test
    void testUpdate_WithMLPrediction_Competition() {
        Event details = Event.builder().title("Updated").type(EventType.COMPETITION)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Medium").teachingStyle("Mixed").build();
        MLPredictionClient.PredictionResponse resp = new MLPredictionClient.PredictionResponse();
        resp.setEfficiency_probability(0.90);
        when(mlPredictionClient.predict(any())).thenReturn(resp);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        assertEquals(0.90, testEvent.getEfficiencyPrediction());
    }

    @Test
    void testUpdate_MLPredictionFails() {
        Event details = Event.builder().title("Updated").type(EventType.WORKSHOP)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Hard").teachingStyle("Visual").build();
        when(mlPredictionClient.predict(any())).thenThrow(new RuntimeException("ML down"));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        assertDoesNotThrow(() -> eventService.update(1L, details, null));
    }

    @Test
    void testUpdate_MLPredictionNullResponse() {
        Event details = Event.builder().title("Updated").type(EventType.WORKSHOP)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Easy").teachingStyle("Visual").build();
        when(mlPredictionClient.predict(any())).thenReturn(null);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        assertNull(testEvent.getEfficiencyPrediction());
    }

    @Test
    void testUpdate_MLPredictionSkipped_NullDifficulty() {
        Event details = Event.builder().title("Updated").type(EventType.WORKSHOP)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty(null).teachingStyle("Visual").build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        verify(mlPredictionClient, never()).predict(any());
    }

    @Test
    void testUpdate_MLPredictionSkipped_NullType() {
        Event details = Event.builder().title("Updated").type(null)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Medium").teachingStyle("Visual").build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        verify(mlPredictionClient, never()).predict(any());
    }

    @Test
    void testUpdate_MLPredictionSkipped_NullTeachingStyle() {
        Event details = Event.builder().title("Updated").type(EventType.WORKSHOP)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Medium").teachingStyle(null).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        verify(mlPredictionClient, never()).predict(any());
    }

    @Test
    void testUpdate_SetsNewFields() {
        Event details = Event.builder().title("New").type(EventType.WORKSHOP)
                .startDate(testEvent.getStartDate()).endDate(testEvent.getEndDate())
                .difficulty("Easy").teachingStyle("Kinesthetic").build();
        when(mlPredictionClient.predict(any())).thenReturn(null);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any())).thenReturn(testEvent);
        eventService.update(1L, details, null);
        assertEquals("Easy", testEvent.getDifficulty());
        assertEquals("Kinesthetic", testEvent.getTeachingStyle());
    }

    // ── delete ──
    @Test
    void testDeleteById_Success() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        eventService.deleteById(1L);
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(eventRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteById(1L));
    }

    // ── findByClubId, findByStatus, findByType ──
    @Test
    void testFindByClubId() {
        when(eventRepository.findByClubId(10L)).thenReturn(List.of(testEvent));
        assertEquals(1, eventService.findByClubId(10L).size());
    }

    @Test
    void testFindByStatus() {
        when(eventRepository.findByStatus("ACTIVE")).thenReturn(List.of(testEvent));
        assertEquals(1, eventService.findByStatus("ACTIVE").size());
    }

    @Test
    void testFindByType() {
        when(eventRepository.findByType(EventType.WORKSHOP)).thenReturn(List.of(testEvent));
        assertEquals(1, eventService.findByType(EventType.WORKSHOP).size());
    }

    // ── registerForEvent ──
    @Test
    void testRegisterForEvent_Confirmed() {
        testEvent.setMaxParticipants(5);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        EventRegistration saved = EventRegistration.builder().id(10L).status(RegistrationStatus.CONFIRMED).build();
        when(eventRegistrationRepository.save(any())).thenReturn(saved);
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        EventRegistration result = eventService.registerForEvent(1L, mockDto);
        assertEquals(RegistrationStatus.CONFIRMED, result.getStatus());
        assertEquals(4, testEvent.getMaxParticipants());
    }

    @Test
    void testRegisterForEvent_Waitlisted() {
        testEvent.setMaxParticipants(0);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(eventRegistrationRepository.save(any())).thenAnswer(i -> {
            EventRegistration r = i.getArgument(0); r.setId(11L); return r;
        });
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        EventRegistration result = eventService.registerForEvent(1L, mockDto);
        assertEquals(RegistrationStatus.WAITLISTED, result.getStatus());
    }

    @Test
    void testRegisterForEvent_NullMaxParticipants() {
        testEvent.setMaxParticipants(null);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(eventRegistrationRepository.save(any())).thenAnswer(i -> {
            EventRegistration r = i.getArgument(0); r.setId(12L); return r;
        });
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        EventRegistration result = eventService.registerForEvent(1L, mockDto);
        assertEquals(RegistrationStatus.WAITLISTED, result.getStatus());
    }

    @Test
    void testRegisterForEvent_AlreadyRegistered() {
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(true);
        assertThrows(BadRequestException.class, () -> eventService.registerForEvent(1L, mockDto));
    }

    @Test
    void testRegisterForEvent_EventNotFound() {
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventService.registerForEvent(1L, mockDto));
    }

    @Test
    void testRegisterForEvent_NullUserId() {
        mockDto.setUserId(null);
        testEvent.setMaxParticipants(5);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        EventRegistration saved = EventRegistration.builder().id(13L).status(RegistrationStatus.CONFIRMED).build();
        when(eventRegistrationRepository.save(any())).thenReturn(saved);
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        assertDoesNotThrow(() -> eventService.registerForEvent(1L, mockDto));
    }

    // ── cancelRegistration ──
    @Test
    void testCancelRegistration_Confirmed_NoWaitlist() {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L).status(RegistrationStatus.CONFIRMED).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(eventRegistrationRepository.findByEventIdAndStatusOrderByRegistrationDateAsc(1L, RegistrationStatus.WAITLISTED))
                .thenReturn(Collections.emptyList());
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        eventService.cancelRegistration(1L);
        verify(eventRegistrationRepository).delete(reg);
    }

    @Test
    void testCancelRegistration_Confirmed_WithWaitlistPromotion() {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L).status(RegistrationStatus.CONFIRMED).build();
        EventRegistration waitlisted = EventRegistration.builder().id(2L).eventId(1L).status(RegistrationStatus.WAITLISTED).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(eventRegistrationRepository.findByEventIdAndStatusOrderByRegistrationDateAsc(1L, RegistrationStatus.WAITLISTED))
                .thenReturn(List.of(waitlisted));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        eventService.cancelRegistration(1L);
        assertEquals(RegistrationStatus.CONFIRMED, waitlisted.getStatus());
        verify(ticketService).generateAndSendTicket(testEvent, waitlisted);
    }

    @Test
    void testCancelRegistration_Confirmed_WaitlistPromotion_EventNotFound() {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L).status(RegistrationStatus.CONFIRMED).build();
        EventRegistration waitlisted = EventRegistration.builder().id(2L).eventId(1L).status(RegistrationStatus.WAITLISTED).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(eventRegistrationRepository.findByEventIdAndStatusOrderByRegistrationDateAsc(1L, RegistrationStatus.WAITLISTED))
                .thenReturn(List.of(waitlisted));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        eventService.cancelRegistration(1L);
        verify(ticketService, never()).generateAndSendTicket(any(), any());
    }

    @Test
    void testCancelRegistration_Waitlisted() {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L).status(RegistrationStatus.WAITLISTED).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        eventService.cancelRegistration(1L);
        verify(eventRegistrationRepository).delete(reg);
        verify(eventRegistrationRepository, never()).findByEventIdAndStatusOrderByRegistrationDateAsc(any(), any());
    }

    @Test
    void testCancelRegistration_NotFound() {
        when(eventRegistrationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventService.cancelRegistration(99L));
    }

    // ── promoteFromWaitlist ──
    @Test
    void testPromoteFromWaitlist_Success() {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L).status(RegistrationStatus.WAITLISTED).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        eventService.promoteFromWaitlist(1L);
        assertEquals(RegistrationStatus.CONFIRMED, reg.getStatus());
        verify(ticketService).generateAndSendTicket(testEvent, reg);
    }

    @Test
    void testPromoteFromWaitlist_EventNotFound() {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L).status(RegistrationStatus.WAITLISTED).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        eventService.promoteFromWaitlist(1L);
        verify(ticketService, never()).generateAndSendTicket(any(), any());
    }

    @Test
    void testPromoteFromWaitlist_NotWaitlisted() {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L).status(RegistrationStatus.CONFIRMED).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        assertThrows(BadRequestException.class, () -> eventService.promoteFromWaitlist(1L));
    }

    @Test
    void testPromoteFromWaitlist_NotFound() {
        when(eventRegistrationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventService.promoteFromWaitlist(99L));
    }

    // ── stats ──
    @Test
    void testGetEventStats_Empty() {
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        EventStatsDTO stats = eventService.getEventStats(1L);
        assertEquals(0, stats.getTotalInscribed());
    }

    @Test
    void testGetEventStats_WithData() {
        EventRegistration r1 = EventRegistration.builder().eventId(1L).status(RegistrationStatus.CONFIRMED)
                .attended(true).feedbackRating(4).discoverySource(DiscoverySource.EMAIL)
                .gender(Gender.MALE).specialty("CS").paymentMethod(PaymentMethod.CASH)
                .participationMode("ONLINE").build();
        EventRegistration r2 = EventRegistration.builder().eventId(1L).status(RegistrationStatus.WAITLISTED)
                .attended(false).feedbackRating(3).discoverySource(DiscoverySource.FRIEND)
                .gender(Gender.FEMALE).specialty("IT").paymentMethod(PaymentMethod.CARD)
                .participationMode("PRESENTIAL").build();
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of(r1, r2));
        EventStatsDTO stats = eventService.getEventStats(1L);
        assertEquals(2, stats.getTotalInscribed());
        assertEquals(1, stats.getConfirmedCount());
        assertEquals(1, stats.getTotalAttended());
        assertEquals(3.5, stats.getAverageRating());
    }

    @Test
    void testGetEventStats_NullFields() {
        EventRegistration r = EventRegistration.builder().eventId(1L).status(RegistrationStatus.CONFIRMED)
                .attended(null).feedbackRating(null).discoverySource(null)
                .gender(null).specialty(null).paymentMethod(null).participationMode(null).build();
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of(r));
        EventStatsDTO stats = eventService.getEventStats(1L);
        assertEquals(1, stats.getTotalInscribed());
        assertEquals(0, stats.getTotalAttended());
        assertEquals(0.0, stats.getAverageRating());
    }

    @Test
    void testGetGlobalStats_WithTopEvents() {
        EventRegistration r = EventRegistration.builder().eventId(1L).status(RegistrationStatus.CONFIRMED)
                .attended(true).discoverySource(DiscoverySource.FACEBOOK).gender(Gender.FEMALE)
                .specialty("IT").paymentMethod(PaymentMethod.CARD).participationMode("PRESENTIAL").build();
        when(eventRegistrationRepository.findAll()).thenReturn(List.of(r));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        EventStatsDTO stats = eventService.getGlobalStats();
        assertNotNull(stats.getTopEvents());
        assertFalse(stats.getTopEvents().isEmpty());
        assertEquals("Test Workshop", stats.getTopEvents().get(0).getTitle());
    }

    @Test
    void testGetGlobalStats_EventNotFoundForTitle() {
        EventRegistration r = EventRegistration.builder().eventId(99L).status(RegistrationStatus.CONFIRMED).build();
        when(eventRegistrationRepository.findAll()).thenReturn(List.of(r));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        EventStatsDTO stats = eventService.getGlobalStats();
        assertEquals("Event #99", stats.getTopEvents().get(0).getTitle());
    }

    @Test
    void testGetGlobalStats_NullEventId() {
        EventRegistration r = EventRegistration.builder().eventId(null).status(RegistrationStatus.CONFIRMED).build();
        when(eventRegistrationRepository.findAll()).thenReturn(List.of(r));
        EventStatsDTO stats = eventService.getGlobalStats();
        assertTrue(stats.getTopEvents().isEmpty());
    }

    @Test
    void testGetBudgetStats() {
        when(eventRepository.findAll()).thenReturn(List.of(testEvent));
        when(eventRepository.count()).thenReturn(1L);
        BudgetStatsDTO stats = eventService.getBudgetStats();
        assertEquals(100.0, stats.getTotalEstimatedCost());
        assertEquals(1, stats.getActiveEventsCount());
    }

    @Test
    void testGetBudgetStats_NullCost() {
        testEvent.setEstimatedCost(null);
        when(eventRepository.findAll()).thenReturn(List.of(testEvent));
        when(eventRepository.count()).thenReturn(1L);
        BudgetStatsDTO stats = eventService.getBudgetStats();
        assertEquals(0.0, stats.getTotalEstimatedCost());
    }

    // ── other queries ──
    @Test
    void testGetRegistrationById() {
        EventRegistration reg = EventRegistration.builder().id(1L).build();
        when(eventRegistrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        assertTrue(eventService.getRegistrationById(1L).isPresent());
    }

    @Test
    void testIsUserRegistered() {
        when(eventRegistrationRepository.existsByEventIdAndUserId(1L, 100L)).thenReturn(true);
        assertTrue(eventService.isUserRegistered(1L, 100L));
    }

    @Test
    void testGetUserRegistrations() {
        when(eventRegistrationRepository.findByUserId(100L)).thenReturn(List.of());
        assertTrue(eventService.getUserRegistrations(100L).isEmpty());
    }

    @Test
    void testGetRegistrationsByEventAndStatus() {
        when(eventRegistrationRepository.findByEventIdAndStatusOrderByRegistrationDateAsc(1L, RegistrationStatus.WAITLISTED))
                .thenReturn(List.of());
        assertTrue(eventService.getRegistrationsByEventAndStatus(1L, RegistrationStatus.WAITLISTED).isEmpty());
    }

    // ── broadcastEventStats ──
    @Test
    void testBroadcastEventStats() {
        when(eventRegistrationRepository.findByEventId(1L)).thenReturn(List.of());
        when(eventRegistrationRepository.findAll()).thenReturn(List.of());
        eventService.broadcastEventStats(1L);
        verify(messagingTemplate).convertAndSend(eq("/topic/event-stats/1"), any(EventStatsDTO.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/event-stats/global"), any(EventStatsDTO.class));
    }

    @Test
    void testHandleWebSocketSubscribeListener_CorrectDestination() {
        org.springframework.web.socket.messaging.SessionSubscribeEvent mockEvent = mock(org.springframework.web.socket.messaging.SessionSubscribeEvent.class);
        org.springframework.messaging.Message<byte[]> mockMessage = mock(org.springframework.messaging.Message.class);
        org.springframework.messaging.MessageHeaders mockHeaders = new org.springframework.messaging.MessageHeaders(Map.of(
                "simpDestination", "/topic/budget-stats",
                "simpMessageType", org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE
        ));

        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getHeaders()).thenReturn(mockHeaders);
        when(eventRepository.findAll()).thenReturn(List.of());

        eventService.handleWebSocketSubscribeListener(mockEvent);

        verify(messagingTemplate).convertAndSend(eq("/topic/budget-stats"), any(BudgetStatsDTO.class));
    }

    @Test
    void testHandleWebSocketSubscribeListener_WrongDestination() {
        org.springframework.web.socket.messaging.SessionSubscribeEvent mockEvent = mock(org.springframework.web.socket.messaging.SessionSubscribeEvent.class);
        org.springframework.messaging.Message<byte[]> mockMessage = mock(org.springframework.messaging.Message.class);
        org.springframework.messaging.MessageHeaders mockHeaders = new org.springframework.messaging.MessageHeaders(Map.of(
                "simpDestination", "/topic/other",
                "simpMessageType", org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE
        ));

        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getHeaders()).thenReturn(mockHeaders);

        eventService.handleWebSocketSubscribeListener(mockEvent);

        verify(messagingTemplate, never()).convertAndSend(eq("/topic/budget-stats"), any(BudgetStatsDTO.class));
    }
}
