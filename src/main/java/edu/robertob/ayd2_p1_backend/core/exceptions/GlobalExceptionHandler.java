package edu.robertob.ayd2_p1_backend.core.exceptions;

import edu.robertob.ayd2_p1_backend.core.models.entities.response.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 401 – credenciales incorrectas en el login */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorDTO handleBadCredentials(BadCredentialsException ex) {
        return new ErrorDTO("Credenciales incorrectas");
    }

    /** 404 – recurso no encontrado */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleNotFound(NotFoundException ex) {
        return new ErrorDTO(ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleNoResourceFound(NoResourceFoundException ex) {
        return new ErrorDTO("Recurso no encontrado");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorDTO handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return new ErrorDTO("Método HTTP no permitido para este recurso");
    }

    /** 409 – recurso duplicado */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO handleDuplicate(DuplicateResourceException ex) {
        return new ErrorDTO(ex.getMessage());
    }

    /** 400 – validación de campos (@Valid) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ErrorDTO(message);
    }

    /** 400 – cuerpo JSON inválido o mal formado */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return new ErrorDTO("Cuerpo de solicitud inválido");
    }

    /** 500 – cualquier otro error no controlado */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO handleGeneric(Exception ex) {
        log.error("Error no controlado en la API", ex);
        return new ErrorDTO("Error interno del servidor");
    }
}
