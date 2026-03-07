package edu.robertob.ayd2_p1_backend.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeMessageEnumTest {

    @Test
    void allValues_haveNonNullCodeAndMessage() {
        for (ErrorCodeMessageEnum entry : ErrorCodeMessageEnum.values()) {
            assertNotNull(entry.getCode(),
                    "Code should not be null for: " + entry.name());
            assertFalse(entry.getCode().isBlank(),
                    "Code should not be blank for: " + entry.name());
            assertNotNull(entry.getMessage(),
                    "Message should not be null for: " + entry.name());
            assertFalse(entry.getMessage().isBlank(),
                    "Message should not be blank for: " + entry.name());
        }
    }

    @Test
    void jwtErrors_haveExpectedCodes() {
        assertEquals("JWT-001", ErrorCodeMessageEnum.JWT_INVALID.getCode());
        assertEquals("JWT-011", ErrorCodeMessageEnum.JWT_EXPIRED.getCode());
        assertEquals("JWT-007", ErrorCodeMessageEnum.JWT_NO_USERNAME.getCode());
    }

    @Test
    void fileErrors_haveExpectedCodes() {
        assertEquals("FILE-001", ErrorCodeMessageEnum.FILE_ALREADY_EXISTS.getCode());
        assertEquals("FILE-005", ErrorCodeMessageEnum.FILE_IO_EXCEPTION.getCode());
    }
}
