package tn.esprit.eventservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.eventservice.dto.*;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.service.EventService;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllEvents() throws Exception {
        Event event = Event.builder().id(1L).title("Integration Test").build();
        when(eventService.findAll()).thenReturn(Arrays.asList(event));
        mockMvc.perform(get("/api/events")).andExpect(status().isOk());
    }

    @Test
    void testGetEventById() throws Exception {
        Event event = Event.builder().id(1L).title("Specific Event").build();
        when(eventService.findById(1L)).thenReturn(Optional.of(event));
        mockMvc.perform(get("/api/events/1")).andExpect(status().isOk());
    }

    @Test
    void testCreateEvent() throws Exception {
        EventDTO dto = EventDTO.builder().title("New").type(EventType.WORKSHOP).build();
        Event saved = Event.builder().id(10L).title("New").build();
        when(eventService.create(any(Event.class), any())).thenReturn(saved);
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdateEvent() throws Exception {
        EventDTO dto = EventDTO.builder().title("Updated").build();
        Event updated = Event.builder().id(1L).title("Updated").build();
        when(eventService.update(any(Long.class), any(Event.class), any())).thenReturn(updated);
        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterForEvent() throws Exception {
        EventRegistrationDto dto = EventRegistrationDto.builder().userName("Eya").build();
        EventRegistration reg = EventRegistration.builder().id(1L).userName("Eya").build();
        when(eventService.registerForEvent(any(), any())).thenReturn(reg);
        mockMvc.perform(post("/api/events/1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStats() throws Exception {
        when(eventService.getEventStats(1L)).thenReturn(EventStatsDTO.builder().build());
        mockMvc.perform(get("/api/events/1/stats")).andExpect(status().isOk());
    }

    @Test
    void testGetGlobalStats() throws Exception {
        when(eventService.getGlobalStats()).thenReturn(EventStatsDTO.builder().build());
        mockMvc.perform(get("/api/events/stats/global")).andExpect(status().isOk());
    }

    @Test
    void testGetBudgetStats() throws Exception {
        when(eventService.getBudgetStats()).thenReturn(BudgetStatsDTO.builder().build());
        mockMvc.perform(get("/api/events/stats/budget")).andExpect(status().isOk());
    }

    @Test
    void testDeleteEvent() throws Exception {
        mockMvc.perform(delete("/api/events/1")).andExpect(status().isNoContent());
    }

    @Test
    void testGetEventById_NotFound() throws Exception {
        when(eventService.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/events/99")).andExpect(status().isNotFound());
    }
}
