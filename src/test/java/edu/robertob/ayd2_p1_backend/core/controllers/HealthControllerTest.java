package edu.robertob.ayd2_p1_backend.core.controllers;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    void health_returnsOkStatusMap() {
        Map<String, String> result = healthController.health();

        assertNotNull(result);
        assertEquals("ok", result.get("status"));
    }
}
