package edu.robertob.ayd2_p1_backend.core.config;

import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneConfigTest {

    @Test
    void init_setsGuatemalaAsDefaultTimeZone() {
        TimeZoneConfig config = new TimeZoneConfig();
        config.init();

        assertEquals("America/Guatemala", TimeZone.getDefault().getID());
    }
}
