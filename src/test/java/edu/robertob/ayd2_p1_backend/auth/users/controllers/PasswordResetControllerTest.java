package edu.robertob.ayd2_p1_backend.auth.users.controllers;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.ForgotPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.ResetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.services.PasswordResetService;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    @Test
    void forgotPassword_delegatesToService() {
        ForgotPasswordDTO dto = mock(ForgotPasswordDTO.class);
        doNothing().when(passwordResetService).requestPasswordReset(dto);

        assertDoesNotThrow(() -> passwordResetController.forgotPassword(dto));

        verify(passwordResetService).requestPasswordReset(dto);
    }

    @Test
    void resetPassword_delegatesToService() throws InvalidTokenException {
        ResetPasswordDTO dto = mock(ResetPasswordDTO.class);
        doNothing().when(passwordResetService).resetPassword(dto);

        assertDoesNotThrow(() -> passwordResetController.resetPassword(dto));

        verify(passwordResetService).resetPassword(dto);
    }

    @Test
    void resetPassword_serviceThrowsInvalidToken_propagatesException() throws InvalidTokenException {
        ResetPasswordDTO dto = mock(ResetPasswordDTO.class);
        doThrow(new InvalidTokenException("Token inválido"))
                .when(passwordResetService).resetPassword(dto);

        assertThrows(InvalidTokenException.class, () -> passwordResetController.resetPassword(dto));
    }
}
