package edu.robertob.ayd2_p1_backend.auth.jwt.filter;

import edu.robertob.ayd2_p1_backend.auth.jwt.utils.JwtTokenInspector;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenInspector jwtTokenInspector;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_publicEndpoint_skipsValidationAndPassesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/health");
        when(request.getMethod()).thenReturn("GET");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenInspector);
    }

    @Test
    void doFilter_publicPostEndpoint_skipsValidationAndPassesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/login");
        when(request.getMethod()).thenReturn("POST");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenInspector);
    }

    @Test
    void doFilter_noAuthorizationHeader_passesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenInspector);
    }

    @Test
    void doFilter_nonBearerHeader_passesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenInspector);
    }

    @Test
    void doFilter_validBearerToken_authenticatesUser() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtTokenInspector.extractUsername("valid.jwt.token")).thenReturn("alice");
        when(jwtTokenInspector.extractUserType("valid.jwt.token")).thenReturn("ROLE_SYSTEM_ADMIN");
        when(jwtTokenInspector.isTokenValid("valid.jwt.token")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // SecurityContext should be populated after valid auth
        org.springframework.security.core.Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        org.junit.jupiter.api.Assertions.assertNotNull(auth);
        org.junit.jupiter.api.Assertions.assertEquals("alice", auth.getName());
    }

    @Test
    void doFilter_nullUsername_doesNotAuthenticate() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer null.user.token");
        when(jwtTokenInspector.extractUsername("null.user.token")).thenReturn(null);
        when(jwtTokenInspector.extractUserType("null.user.token")).thenReturn("ROLE_DEVELOPER");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        org.junit.jupiter.api.Assertions.assertNull(
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_invalidToken_writes401Response() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtTokenInspector.extractUsername("bad.token"))
                .thenThrow(new InvalidTokenException("Token inválido"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_tokenAlreadyAuthenticated_skipsNewAuth() throws Exception {
        // Pre-populate the SecurityContext as if already authenticated
        org.springframework.security.core.userdetails.UserDetails existingUser =
                new org.springframework.security.core.userdetails.User(
                        "existing", "", java.util.List.of());
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer some.token");
        when(jwtTokenInspector.extractUsername("some.token")).thenReturn("existing");
        when(jwtTokenInspector.extractUserType("some.token")).thenReturn("ROLE_DEVELOPER");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Should not call isTokenValid because auth was already present
        verify(jwtTokenInspector, never()).isTokenValid(anyString());
    }
}
