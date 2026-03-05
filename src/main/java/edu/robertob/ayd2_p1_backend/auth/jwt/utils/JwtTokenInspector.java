package edu.robertob.ayd2_p1_backend.auth.jwt.utils;

import java.util.Date;

import edu.robertob.ayd2_p1_backend.auth.jwt.models.JwtConfig;
import org.springframework.stereotype.Component;

import edu.robertob.ayd2_p1_backend.auth.jwt.models.JwtConfig;
import edu.robertob.ayd2_p1_backend.auth.jwt.services.JwtGeneratorService;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.enums.JwtErrorEnum;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.RequiredTypeException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenInspector {

    private final JwtConfig jwtConfig;

    public String extractUserType(String token) throws InvalidTokenException {
        try {

            String userType = extractAllClaims(token).get(JwtGeneratorService.CLAIM_NAME_USER_TYPE, String.class);

            if (userType == null) {
                throw JwtErrorEnum.JWT_NO_USER_TYPE.getInvalidTokenException();
            }
            return userType;

        } catch (RequiredTypeException ex) {
            throw JwtErrorEnum.CLAIM_TYPE_MISMATCH.getInvalidTokenException();

        } catch (InvalidTokenException ex) {
            throw ex;
        }
    }

    /**
     * Extrae y devuelve el nombre de usuario del token jwt.
     *
     * @param token el token jwt del cual se extraerá el nombre de usuario.
     * @return el nombre de usuario contenido en el token.
     */
    public String extractUsername(String token) throws InvalidTokenException {

        String username = extractAllClaims(token).getSubject();

        if (username == null) {
            throw JwtErrorEnum.JWT_NO_USERNAME.getInvalidTokenException();
        }

        return username;

    }

    /**
     * Verifica si expiracion del token es antes que la fecha actual
     *
     * @param token
     * @return
     */
    public Boolean isTokenExpired(String token) throws InvalidTokenException {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae y devuelve la fecha de expiración del token jwt.
     *
     * @param token el token jwt del cual se extraerá la fecha de expiración.
     * @return la fecha de expiración contenida en el token.
     */
    public Date extractExpiration(String token) throws InvalidTokenException {

        Date expiration = extractAllClaims(token).getExpiration();

        if (expiration == null) {// si viene nulo entonces debemos decir error al usuario
            throw JwtErrorEnum.JWT_NO_EXPIRATION.getInvalidTokenException();
        }

        return expiration;

    }

    /**
     * Valida si un token jwt es valido verificando que el token no haya expirado.
     *
     * @param token el token jwt a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    public Boolean isTokenValid(String token) throws InvalidTokenException {

        return !isTokenExpired(token);

    }

    /**
     * Extrae y devuelve los claims de un token jwt firmado.
     *
     * @param token el token jwt a analizar.
     * @return los claims contenidos en el token.
     */
    private Claims extractAllClaims(String token) throws InvalidTokenException {
        try {
            // crea un parser para validar el jwt
            return Jwts.parserBuilder()
                    // aqui se verifica el jwt con la llave secretea
                    .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.getSecretBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (UnsupportedJwtException e) {
            throw JwtErrorEnum.JWT_UNSUPPORTED.getInvalidTokenException();
        } catch (MalformedJwtException e) {
            throw JwtErrorEnum.JWT_MALFORMED.getInvalidTokenException();
        } catch (SignatureException e) {
            throw JwtErrorEnum.JWT_SIGNATURE_INVALID.getInvalidTokenException();
        } catch (ExpiredJwtException e) {
            throw JwtErrorEnum.JWT_EXPIRED.getInvalidTokenException();
        } catch (IllegalArgumentException e) {
            throw JwtErrorEnum.JWT_ILLEGAL_ARGUMENT.getInvalidTokenException();
        }

    }

}
