package edu.robertob.ayd2_p1_backend.auth.users.controllers;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.ForgotPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.ResetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.services.PasswordResetService;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Requests a password reset email. Always returns 202 Accepted,
     * even when no account is found for the given email, to prevent user enumeration.
     */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@RequestBody @Valid ForgotPasswordDTO dto) {
        passwordResetService.requestPasswordReset(dto);
    }

    /**
     * Resets the user's password using the token received by email.
     */
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody @Valid ResetPasswordDTO dto) throws InvalidTokenException {
        passwordResetService.resetPassword(dto);
    }
}
