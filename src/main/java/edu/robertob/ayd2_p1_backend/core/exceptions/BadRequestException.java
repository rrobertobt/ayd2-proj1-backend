package edu.robertob.ayd2_p1_backend.core.exceptions;

public class BadRequestException extends CustomRuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
