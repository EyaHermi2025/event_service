package tn.esprit.eventservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventType;
import tn.esprit.eventservice.service.EventService;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    public void testGetAllEvents() throws Exception {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Integration Test Event");
        event.setType(EventType.COMPETITION);

        when(eventService.findAll()).thenReturn(Arrays.asList(event));

        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Title").value("Integration Test Event"));
    }

    @Test
    public void testGetEventById_Found() throws Exception {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Specific Event");

        when(eventService.findById(1L)).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Title").value("Specific Event"));
    }

    @Test
    public void testGetEventById_NotFound() throws Exception {
        when(eventService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
