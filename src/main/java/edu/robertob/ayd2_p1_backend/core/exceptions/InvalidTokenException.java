package edu.robertob.ayd2_p1_backend.core.exceptions;

public class InvalidTokenException extends CustomRuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

}