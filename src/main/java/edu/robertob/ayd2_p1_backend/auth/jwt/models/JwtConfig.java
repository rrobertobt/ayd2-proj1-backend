package edu.robertob.ayd2_p1_backend.auth.jwt.models;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    public byte[] getSecretBytes() {
        // Convertir la clave secreta a bytes usando base64 para asegurar que tenga la longitud adecuada para el algoritmo de firma
        if (secretKey.length() < 32) {
            // Si la clave es demasiado corta, rellenarla con ceros hasta alcanzar los 32 caracteres
            secretKey = String.format("%-32s", secretKey).replace(' ', '0');
        }

        return Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));

    }
}
