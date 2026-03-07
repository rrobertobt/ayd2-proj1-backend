package edu.robertob.ayd2_p1_backend.core.enums;

import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtErrorEnumTest {

    @Test
    void allValues_haveNonNullInvalidTokenException() {
        for (JwtErrorEnum entry : JwtErrorEnum.values()) {
            assertNotNull(entry.getInvalidTokenException(),
                    "InvalidTokenException should not be null for: " + entry.name());
        }
    }

    @Test
    void jwtExpired_exceptionMessageIsNonEmpty() {
        InvalidTokenException ex = JwtErrorEnum.JWT_EXPIRED.getInvalidTokenException();
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    void jwtMalformed_exceptionMessageIsNonEmpty() {
        InvalidTokenException ex = JwtErrorEnum.JWT_MALFORMED.getInvalidTokenException();
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    void jwtNoUsername_exceptionMessageContainsCode() {
        InvalidTokenException ex = JwtErrorEnum.JWT_NO_USERNAME.getInvalidTokenException();
        // The message should be the error code from ErrorCodeMessageEnum
        assertEquals(ErrorCodeMessageEnum.JWT_NO_USERNAME.getCode(), ex.getMessage());
    }

    @Test
    void claimTypeMismatch_exceptionMessageContainsCode() {
        InvalidTokenException ex = JwtErrorEnum.CLAIM_TYPE_MISMATCH.getInvalidTokenException();
        assertEquals(ErrorCodeMessageEnum.CLAIM_TYPE_MISMATCH.getCode(), ex.getMessage());
    }
}
