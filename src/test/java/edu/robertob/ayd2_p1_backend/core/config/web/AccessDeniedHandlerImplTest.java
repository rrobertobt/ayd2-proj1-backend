package edu.robertob.ayd2_p1_backend.core.config.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessDeniedHandlerImplTest {

    @Test
    void handle_writes403StatusAndJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl(objectMapper);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        AccessDeniedException exception = new AccessDeniedException("Access denied");
        handler.handle(request, response, exception);

        verify(response).setStatus(403);
        verify(response).setContentType("application/json");
        String written = sw.toString();
        assertTrue(written.contains("Acceso denegado"));
    }
}
