package edu.robertob.ayd2_p1_backend.core.exceptions;

import edu.robertob.ayd2_p1_backend.core.models.entities.response.ErrorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadCredentials_returnsUnauthorizedErrorDTO() {
        BadCredentialsException ex = new BadCredentialsException("wrong password");

        ErrorDTO result = handler.handleBadCredentials(ex);

        assertNotNull(result);
        assertEquals("Credenciales incorrectas", result.message());
    }

    @Test
    void handleNotFound_returnsMessageInErrorDTO() {
        NotFoundException ex = new NotFoundException("User not found");

        ErrorDTO result = handler.handleNotFound(ex);

        assertNotNull(result);
        assertEquals("User not found", result.message());
    }

    @Test
    void handleNoResourceFound_returnsGenericNotFoundMessage() throws Exception {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/unknown");

        ErrorDTO result = handler.handleNoResourceFound(ex);

        assertNotNull(result);
        assertEquals("Recurso no encontrado", result.message());
    }

    @Test
    void handleMethodNotAllowed_returnsMethodNotAllowedMessage() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("DELETE");

        ErrorDTO result = handler.handleMethodNotAllowed(ex);

        assertNotNull(result);
        assertEquals("Método HTTP no permitido para este recurso", result.message());
    }

    @Test
    void handleDuplicate_returnsMessageInErrorDTO() {
        DuplicateResourceException ex = new DuplicateResourceException("Email already exists");

        ErrorDTO result = handler.handleDuplicate(ex);

        assertNotNull(result);
        assertEquals("Email already exists", result.message());
    }

    @Test
    void handleInvalidToken_returnsMessageInErrorDTO() {
        InvalidTokenException ex = new InvalidTokenException("Token expired");

        ErrorDTO result = handler.handleInvalidToken(ex);

        assertNotNull(result);
        assertEquals("Token expired", result.message());
    }

    @Test
    void handleValidation_returnsFieldErrorsJoined() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "username", "must not be blank"));
        bindingResult.addError(new FieldError("target", "email", "invalid format"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ErrorDTO result = handler.handleValidation(ex);

        assertNotNull(result);
        assertTrue(result.message().contains("username: must not be blank"));
        assertTrue(result.message().contains("email: invalid format"));
    }

    @Test
    void handleUnreadableMessage_returnsBadRequestMessage() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("bad json",
                        new MockHttpInputMessage(new byte[0]));

        ErrorDTO result = handler.handleUnreadableMessage(ex);

        assertNotNull(result);
        assertEquals("Cuerpo de solicitud inválido", result.message());
    }

    @Test
    void handleGeneric_returnsGenericInternalServerErrorMessage() {
        Exception ex = new RuntimeException("Some unexpected error");

        ErrorDTO result = handler.handleGeneric(ex);

        assertNotNull(result);
        assertEquals("Error interno del servidor", result.message());
    }
}
