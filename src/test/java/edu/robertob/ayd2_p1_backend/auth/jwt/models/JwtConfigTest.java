package edu.robertob.ayd2_p1_backend.auth.jwt.models;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    @Test
    void getSecretBytes_longEnoughKey_decodesBase64Correctly() throws Exception {
        String rawKey = "12345678901234567890123456789012"; // exactly 32 bytes
        String base64Key = Base64.getEncoder().encodeToString(rawKey.getBytes(StandardCharsets.UTF_8));

        JwtConfig config = new JwtConfig();
        setSecretField(config, base64Key);

        byte[] result = config.getSecretBytes();

        assertNotNull(result);
        assertArrayEquals(rawKey.getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    void getSecretBytes_shortKey_paddedThen32BytesDecoded() throws Exception {
        // Key shorter than 32 characters — JwtConfig pads it to 32 chars
        String shortRaw = "shortkey"; // 8 chars
        // After padding: "shortkey000000000000000000000000" (32 chars)
        String padded = String.format("%-32s", shortRaw).replace(' ', '0');
        String base64Key = Base64.getEncoder().encodeToString(padded.getBytes(StandardCharsets.UTF_8));

        JwtConfig config = new JwtConfig();
        setSecretField(config, base64Key); // set the already-base64 padded key

        // The secret is already >= 32 chars in length as base64, so no extra padding is applied
        byte[] result = config.getSecretBytes();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void getSecretBytes_shortRawKey_appliesPaddingBeforeDecode() throws Exception {
        // Use a raw short key whose base64 has no trailing '=' so the padded form is valid Base64
        // "abc" base64-encodes to "YWJj" (4 chars, no '=') → < 32, gets padded
        String tinyKey = "abc"; // 3 chars, base64 = "YWJj" (4 chars, no trailing '=')
        String base64TinyKey = Base64.getEncoder().encodeToString(tinyKey.getBytes(StandardCharsets.UTF_8));
        // base64TinyKey == "YWJj"

        JwtConfig config = new JwtConfig();
        setSecretField(config, base64TinyKey);

        // After padding: "YWJj0000000000000000000000000000" (32 chars, valid Base64)
        String paddedKey = String.format("%-32s", base64TinyKey).replace(' ', '0');
        byte[] expected = Base64.getDecoder().decode(paddedKey.getBytes(StandardCharsets.UTF_8));

        byte[] result = config.getSecretBytes();

        assertArrayEquals(expected, result);
    }

    private void setSecretField(JwtConfig config, String value) throws Exception {
        Field field = JwtConfig.class.getDeclaredField("secretKey");
        field.setAccessible(true);
        field.set(config, value);
    }
}
