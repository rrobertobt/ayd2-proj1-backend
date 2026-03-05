package edu.robertob.ayd2_p1_backend.auth.jwt.services;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import edu.robertob.ayd2_p1_backend.auth.jwt.models.JwtConfig;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtGeneratorService {

    private final JwtConfig jwtConfig;

    public static final String CLAIM_NAME_USER_TYPE = "userType";

    private static final Long JWT_TOKEN_VALIDITY_HOURS = 48L;
    private static final Long JWT_TOKEN_TIME_VALIDITY = Duration.ofHours(JWT_TOKEN_VALIDITY_HOURS).toMillis();

    private static final String ROLE_KEY = "ROLE_";

    public String generateToken(UserModel user) {
        System.out.println("Generando token para el usuario: " + user.getUsername() + " con rol: " + user.getRole().getCode().getCode());
        // mandmaos a cargar las clims (las base porue solo esas son necesarias)
        Map<String, Object> claims = new HashMap<>();

        // Agregar el rol del usuario en las autorities
        claims.put(CLAIM_NAME_USER_TYPE, ROLE_KEY + user.getRole().getCode().getCode());
        System.out.println("Claims generadas para el token: " + claims);

        // Generar el token
        String token = createToken(claims, user.getUsername());
        System.out.println("Token JWT generado: " + token);
        return token;
    }

    /**
     * Crea un token JWT firmado a partir de las claims y el nombre de usuario.
     * 
     * El token generado incluye una fecha de emisión, fecha de expiración
     * y se firma utilizando la clave secreta configurada.
     *
     * @param claims   Claims a incluir en el token.
     * @param username Nombre de usuario autenticado.
     * @return Token JWT firmado como cadena.
     */
    private String createToken(Map<String, Object> claims, String username) {
        System.out.println("Creando token JWT para el usuario: " + username + " con claims: " + claims);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_TIME_VALIDITY))
                .signWith(Keys.hmacShaKeyFor(jwtConfig.getSecretBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
