package tn.esprit.eventservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Event", 1L);
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Event not found with id: 1", response.getBody().get("error"));
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad request message");
        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request message", response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new Exception("General error");
        ResponseEntity<Map<String, String>> response = handler.handleGeneral(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("General error"));
    }
}
