package edu.robertob.ayd2_p1_backend.core.config.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthEntryPointTest {

    @Test
    void commence_writes401StatusAndJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        AuthEntryPoint authEntryPoint = new AuthEntryPoint(objectMapper);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        AuthenticationException authException = new BadCredentialsException("Unauthorized");
        authEntryPoint.commence(request, response, authException);

        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        String written = sw.toString();
        assertTrue(written.contains("Acceso no autorizado"));
    }
}
