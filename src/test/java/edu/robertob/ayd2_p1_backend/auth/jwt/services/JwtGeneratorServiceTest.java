package edu.robertob.ayd2_p1_backend.auth.jwt.services;

import edu.robertob.ayd2_p1_backend.auth.jwt.models.JwtConfig;
import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtGeneratorServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtGeneratorService jwtGeneratorService;

    @Test
    void generateToken_shouldIncludeSubjectRoleClaimAndExpiration() {
        byte[] secret = "12345678901234567890123456789012".getBytes();
        UserModel user = buildUser("john", RolesEnum.PROJECT_ADMIN);
        when(jwtConfig.getSecretBytes()).thenReturn(secret);

        String token = jwtGeneratorService.generateToken(user);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("john", claims.getSubject());
        assertEquals("ROLE_PROJECT_ADMIN", claims.get(JwtGeneratorService.CLAIM_NAME_USER_TYPE));
        assertTrue(claims.getIssuedAt().getTime() > 0);
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));

        Date expiration = claims.getExpiration();
        long validityMillis = expiration.getTime() - claims.getIssuedAt().getTime();
        long fortyEightHoursMillis = 48L * 60L * 60L * 1000L;
        assertTrue(validityMillis >= fortyEightHoursMillis - 1500L);
        assertTrue(validityMillis <= fortyEightHoursMillis + 1500L);
    }

    private UserModel buildUser(String username, RolesEnum roleCode) {
        RoleModel role = new RoleModel();
        role.setId(5L);
        role.setCode(roleCode);
        role.setName("Role");

        UserModel user = new UserModel();
        user.setId(1L);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }
}
