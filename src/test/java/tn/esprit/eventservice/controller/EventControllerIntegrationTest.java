package tn.esprit.eventservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tn.esprit.eventservice.dto.*;
import tn.esprit.eventservice.entity.*;
import tn.esprit.eventservice.service.EventService;

@ExtendWith(MockitoExtension.class)
class EventControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    private Event buildEvent(Long id, String title) {
        return Event.builder().id(id).title(title).type(EventType.WORKSHOP)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .clubId(1L).maxParticipants(50).build();
    }

    @Test
    void testGetAll() throws Exception {
        when(eventService.findAll()).thenReturn(List.of(buildEvent(1L, "E1")));
        mockMvc.perform(get("/api/events")).andExpect(status().isOk());
    }

    @Test
    void testGetById_Found() throws Exception {
        when(eventService.findById(1L)).thenReturn(Optional.of(buildEvent(1L, "E1")));
        mockMvc.perform(get("/api/events/1")).andExpect(status().isOk());
    }

    @Test
    void testGetById_NotFound() throws Exception {
        when(eventService.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/events/99")).andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        EventDTO dto = EventDTO.builder().title("New").type(EventType.WORKSHOP).build();
        Event saved = buildEvent(10L, "New");
        when(eventService.create(any(Event.class), any())).thenReturn(saved);
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdate() throws Exception {
        EventDTO dto = EventDTO.builder().title("Updated").build();
        Event updated = buildEvent(1L, "Updated");
        when(eventService.update(any(Long.class), any(Event.class), any())).thenReturn(updated);
        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/events/1")).andExpect(status().isNoContent());
    }

    @Test
    void testRegisterForEvent() throws Exception {
        EventRegistrationDto dto = EventRegistrationDto.builder().userName("Eya").userEmail("e@t.com").build();
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L)
                .userName("Eya").userEmail("e@t.com").status(RegistrationStatus.CONFIRMED).build();
        when(eventService.registerForEvent(any(), any())).thenReturn(reg);
        when(eventService.findById(1L)).thenReturn(Optional.of(buildEvent(1L, "E")));
        mockMvc.perform(post("/api/events/1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCheckIfRegistered() throws Exception {
        when(eventService.isUserRegistered(1L, 100L)).thenReturn(true);
        mockMvc.perform(get("/api/events/1/is-registered").param("userId", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserRegistrations() throws Exception {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L)
                .userName("U").userEmail("u@t.com").status(RegistrationStatus.CONFIRMED).build();
        when(eventService.getUserRegistrations(100L)).thenReturn(List.of(reg));
        when(eventService.findById(1L)).thenReturn(Optional.of(buildEvent(1L, "E")));
        mockMvc.perform(get("/api/events/my-registrations").param("userId", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetEventStats() throws Exception {
        when(eventService.getEventStats(1L)).thenReturn(EventStatsDTO.builder().totalInscribed(5).build());
        mockMvc.perform(get("/api/events/1/stats")).andExpect(status().isOk());
    }

    @Test
    void testGetGlobalStats() throws Exception {
        when(eventService.getGlobalStats()).thenReturn(EventStatsDTO.builder().build());
        mockMvc.perform(get("/api/events/stats/global")).andExpect(status().isOk());
    }

    @Test
    void testGetBudgetStats() throws Exception {
        when(eventService.getBudgetStats()).thenReturn(BudgetStatsDTO.builder().totalEstimatedCost(500.0).build());
        mockMvc.perform(get("/api/events/budget")).andExpect(status().isOk());
    }

    @Test
    void testGetRegistrationById_Found() throws Exception {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L)
                .userName("U").userEmail("u@t.com").status(RegistrationStatus.CONFIRMED).build();
        when(eventService.getRegistrationById(1L)).thenReturn(Optional.of(reg));
        when(eventService.findById(1L)).thenReturn(Optional.of(buildEvent(1L, "E")));
        mockMvc.perform(get("/api/events/registrations/1")).andExpect(status().isOk());
    }

    @Test
    void testGetRegistrationById_NotFound() throws Exception {
        when(eventService.getRegistrationById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/events/registrations/99")).andExpect(status().isNotFound());
    }

    @Test
    void testCancelRegistration() throws Exception {
        mockMvc.perform(delete("/api/events/registrations/1")).andExpect(status().isNoContent());
    }

    @Test
    void testGetWaitlist() throws Exception {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(1L)
                .userName("U").userEmail("u@t.com").status(RegistrationStatus.WAITLISTED).build();
        when(eventService.getRegistrationsByEventAndStatus(1L, RegistrationStatus.WAITLISTED))
                .thenReturn(List.of(reg));
        when(eventService.findById(1L)).thenReturn(Optional.of(buildEvent(1L, "E")));
        mockMvc.perform(get("/api/events/1/waitlist")).andExpect(status().isOk());
    }

    @Test
    void testPromoteFromWaitlist() throws Exception {
        mockMvc.perform(post("/api/events/registrations/1/promote")).andExpect(status().isOk());
    }

    @Test
    void testMapRegToDTO_EventNotFound() throws Exception {
        EventRegistration reg = EventRegistration.builder().id(1L).eventId(999L)
                .userName("U").userEmail("u@t.com").status(RegistrationStatus.CONFIRMED).build();
        when(eventService.getRegistrationById(1L)).thenReturn(Optional.of(reg));
        when(eventService.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/events/registrations/1")).andExpect(status().isOk());
    }

    @Test
    void testGetById_MapsNewFields() throws Exception {
        Event event = Event.builder().id(1L).title("ML Event").type(EventType.WORKSHOP)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .clubId(1L).maxParticipants(50)
                .difficulty("Hard")
                .teachingStyle("Visual")
                .efficiencyPrediction(0.88)
                .build();
        when(eventService.findById(1L)).thenReturn(Optional.of(event));
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Difficulty").value("Hard"))
                .andExpect(jsonPath("$.TeachingStyle").value("Visual"))
                .andExpect(jsonPath("$.EfficiencyPrediction").value(0.88));
    }

    @Test
    void testCreateEvent_WithNewFields() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("New ML Event").type(EventType.COMPETITION)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .clubId(2L).maxParticipants(30)
                .difficulty("Medium")
                .teachingStyle("Auditory")
                .efficiencyPrediction(0.75)
                .build();
        Event saved = Event.builder().id(5L).title("New ML Event").type(EventType.COMPETITION)
                .startDate(dto.getStartDate()).endDate(dto.getEndDate())
                .difficulty("Medium").teachingStyle("Auditory").efficiencyPrediction(0.75).build();
        when(eventService.create(any(Event.class), any())).thenReturn(saved);
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
