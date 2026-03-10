package edu.robertob.ayd2_p1_backend.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JacksonConfigTest {

    @Test
    void objectMapper_returnsBuiltMapper() {
        Jackson2ObjectMapperBuilder builder = mock(Jackson2ObjectMapperBuilder.class);
        ObjectMapper expected = new ObjectMapper();
        when(builder.build()).thenReturn(expected);

        JacksonConfig config = new JacksonConfig();
        ObjectMapper result = config.objectMapper(builder);

        assertSame(expected, result);
        verify(builder).build();
    }
}
