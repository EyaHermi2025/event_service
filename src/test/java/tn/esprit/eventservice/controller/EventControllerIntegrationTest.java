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
import tn.esprit.eventservice.dto.EventDTO;
import tn.esprit.eventservice.entity.Event;
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
        Event event = Event.builder()
                .id(1L)
                .title("Integration Test Event")
                .type(EventType.COMPETITION)
                .build();

        when(eventService.findAll()).thenReturn(Arrays.asList(event));

        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Integration Test Event"));
    }

    @Test
    void testGetEventById_Found() throws Exception {
        Event event = Event.builder()
                .id(1L)
                .title("Specific Event")
                .build();

        when(eventService.findById(1L)).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Specific Event"));
    }

    @Test
    void testCreateEvent() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Created Event")
                .type(EventType.WORKSHOP)
                .build();
        
        Event savedEvent = Event.builder()
                .id(10L)
                .title("Created Event")
                .build();

        when(eventService.create(any(Event.class), any())).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Created Event"));
    }

    @Test
    void testUpdateEvent() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Updated Event")
                .build();
        
        Event updatedEvent = Event.builder()
                .id(1L)
                .title("Updated Event")
                .build();

        when(eventService.update(any(Long.class), any(Event.class), any())).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"));
    }

    @Test
    void testDeleteEvent() throws Exception {
        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetEventById_NotFound() throws Exception {
        when(eventService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
