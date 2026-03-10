package edu.robertob.ayd2_p1_backend.core.config.web;

import edu.robertob.ayd2_p1_backend.auth.jwt.filter.JwtAuthenticationFilter;
import edu.robertob.ayd2_p1_backend.core.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock AppProperties appProperties;
    @Mock JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock AuthEntryPoint authEntryPoint;
    @Mock AccessDeniedHandlerImpl accessDeniedHandler;

    @InjectMocks SecurityConfig securityConfig;

    // ── passwordEncoder ───────────────────────────────────────────────────────

    @Test
    void passwordEncoder_returnsBCrypt() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void passwordEncoder_encodeAndMatches() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "secret123";

        String encoded = encoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(encoder.matches(raw, encoded));
        assertFalse(encoder.matches("wrong", encoded));
    }

    // ── corsConfigurationSource ───────────────────────────────────────────────

    @Test
    void corsConfigurationSource_registersAllPaths() {
        when(appProperties.getAllowedOrigins()).thenReturn("http://localhost:3000");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);
        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/users"));
        assertNotNull(config);
    }

    @Test
    void corsConfigurationSource_containsExpectedMethods() {
        when(appProperties.getAllowedOrigins()).thenReturn("http://localhost:3000");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/any"));

        assertNotNull(config);
        assertTrue(config.getAllowedMethods().containsAll(
                java.util.List.of("GET", "POST", "PATCH", "DELETE", "PUT")));
    }

    @Test
    void corsConfigurationSource_containsExpectedHeaders() {
        when(appProperties.getAllowedOrigins()).thenReturn("http://localhost:3000");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/any"));

        assertNotNull(config);
        assertTrue(config.getAllowedHeaders().containsAll(
                java.util.List.of("Authorization", "Content-Type")));
    }

    @Test
    void corsConfigurationSource_usesAllowedOriginsFromAppProperties() {
        when(appProperties.getAllowedOrigins()).thenReturn("https://example.com");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/any"));

        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("https://example.com"));
    }
}
