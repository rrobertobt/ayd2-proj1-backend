package edu.robertob.ayd2_p1_backend.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppPropertiesTest {

    @Test
    void settersAndGetters_workCorrectly() {
        AppProperties props = new AppProperties();

        props.setFrontendHost("http://frontend.example.com");
        props.setBackendHost("http://backend.example.com");

        assertEquals("http://frontend.example.com", props.getFrontendHost());
        assertEquals("http://backend.example.com", props.getBackendHost());
    }
}
