package edu.robertob.ayd2_p1_backend.auth.jwt.utils;

import edu.robertob.ayd2_p1_backend.auth.jwt.models.JwtConfig;
import edu.robertob.ayd2_p1_backend.auth.jwt.services.JwtGeneratorService;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenInspectorTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtTokenInspector jwtTokenInspector;

    private byte[] secret;

    @BeforeEach
    void setUp() {
        secret = "12345678901234567890123456789012".getBytes(); // 32 bytes
        when(jwtConfig.getSecretBytes()).thenReturn(secret);
    }

    // ── extractUsername ───────────────────────────────────────────────────────

    @Test
    void extractUsername_validToken_returnsSubject() {
        String token = buildToken("alice", "ROLE_DEVELOPER", 60_000L);

        String result = jwtTokenInspector.extractUsername(token);

        assertEquals("alice", result);
    }

    @Test
    void extractUsername_malformedToken_throwsInvalidTokenException() {
        assertThrows(InvalidTokenException.class,
                () -> jwtTokenInspector.extractUsername("not.a.jwt.token"));
    }

    // ── extractUserType ───────────────────────────────────────────────────────

    @Test
    void extractUserType_validToken_returnsUserType() {
        String token = buildToken("bob", "ROLE_PROJECT_ADMIN", 60_000L);

        String result = jwtTokenInspector.extractUserType(token);

        assertEquals("ROLE_PROJECT_ADMIN", result);
    }

    @Test
    void extractUserType_missingClaim_throwsInvalidTokenException() {
        // Token without the userType claim
        String token = Jwts.builder()
                .setSubject("noUserType")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(InvalidTokenException.class,
                () -> jwtTokenInspector.extractUserType(token));
    }

    // ── extractExpiration ─────────────────────────────────────────────────────

    @Test
    void extractExpiration_validToken_returnsDate() {
        String token = buildToken("carol", "ROLE_DEVELOPER", 60_000L);

        Date expiration = jwtTokenInspector.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    // ── isTokenExpired ────────────────────────────────────────────────────────

    @Test
    void isTokenExpired_validToken_returnsFalse() {
        String token = buildToken("dave", "ROLE_DEVELOPER", 60_000L);

        Boolean result = jwtTokenInspector.isTokenExpired(token);

        assertFalse(result);
    }

    @Test
    void isTokenExpired_expiredToken_throwsInvalidTokenException() {
        // Build a token that's already expired
        String expiredToken = Jwts.builder()
                .setSubject("eve")
                .claim(JwtGeneratorService.CLAIM_NAME_USER_TYPE, "ROLE_DEVELOPER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10_000L))
                .setExpiration(new Date(System.currentTimeMillis() - 5_000L)) // expired 5 seconds ago
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();

        // ExpiredJwtException is caught internally and re-thrown as InvalidTokenException
        assertThrows(InvalidTokenException.class,
                () -> jwtTokenInspector.isTokenExpired(expiredToken));
    }

    // ── isTokenValid ──────────────────────────────────────────────────────────

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = buildToken("frank", "ROLE_DEVELOPER", 60_000L);

        Boolean result = jwtTokenInspector.isTokenValid(token);

        assertTrue(result);
    }

    @Test
    void isTokenValid_expiredToken_throwsInvalidTokenException() {
        String expiredToken = Jwts.builder()
                .setSubject("grace")
                .claim(JwtGeneratorService.CLAIM_NAME_USER_TYPE, "ROLE_DEVELOPER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10_000L))
                .setExpiration(new Date(System.currentTimeMillis() - 5_000L))
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(InvalidTokenException.class,
                () -> jwtTokenInspector.isTokenValid(expiredToken));
    }

    // ── exception paths for extractAllClaims ─────────────────────────────────

    @Test
    void extractUsername_wrongSignature_throwsInvalidTokenException() {
        byte[] wrongSecret = "99999999999999999999999999999999".getBytes();
        String tokenWithWrongSig = Jwts.builder()
                .setSubject("hacker")
                .claim(JwtGeneratorService.CLAIM_NAME_USER_TYPE, "ROLE_DEVELOPER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(Keys.hmacShaKeyFor(wrongSecret), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(InvalidTokenException.class,
                () -> jwtTokenInspector.extractUsername(tokenWithWrongSig));
    }

    @Test
    void extractUsername_illegalArgument_throwsInvalidTokenException() {
        assertThrows(InvalidTokenException.class,
                () -> jwtTokenInspector.extractUsername(""));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String buildToken(String subject, String userType, long validityMs) {
        return Jwts.builder()
                .setSubject(subject)
                .claim(JwtGeneratorService.CLAIM_NAME_USER_TYPE, userType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();
    }
}
