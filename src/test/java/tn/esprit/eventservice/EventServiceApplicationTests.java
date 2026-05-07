package tn.esprit.eventservice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EventServiceApplicationTests {

    @Test
    void testMainClass() {
        assertDoesNotThrow(() -> {
            // Just verify the class can be instantiated
            EventServiceApplication app = new EventServiceApplication();
            assert app != null;
        });
    }
}
