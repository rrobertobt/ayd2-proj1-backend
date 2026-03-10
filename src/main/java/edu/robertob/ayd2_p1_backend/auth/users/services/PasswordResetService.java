package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.ForgotPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.ResetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.PasswordResetTokenModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.PasswordResetTokenRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import edu.robertob.ayd2_p1_backend.core.config.AppProperties;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class PasswordResetService {

    private static final int RESET_TOKEN_EXPIRATION_HOURS = 1;

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AppProperties appProperties;

    /**
     * Initiates a password reset for the given email address.
     * <p>
     * To prevent user enumeration, this method always returns silently even when
     * no account is associated with the provided email.
     *
     * @param dto request containing the user's email
     */
    public void requestPasswordReset(ForgotPasswordDTO dto) {
        Optional<UserModel> userOptional = userRepository.findByEmail(dto.getEmail());

        if (userOptional.isEmpty()) {
            // Do not reveal whether the email exists
            log.debug("Password reset requested for unknown email: {}", dto.getEmail());
            return;
        }

        UserModel user = userOptional.get();

        if (!user.isActive()) {
            // Silently ignore inactive accounts — no information leaked
            log.debug("Password reset requested for inactive user id={}", user.getId());
            return;
        }

        // Invalidate any existing unused tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String rawToken = generateSecureToken();

        PasswordResetTokenModel tokenModel = new PasswordResetTokenModel();
        tokenModel.setUser(user);
        tokenModel.setToken(rawToken);
        tokenModel.setUsed(false);
        tokenModel.setExpiresAt(Instant.now().plus(RESET_TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS));
        passwordResetTokenRepository.save(tokenModel);

        String resetLink = appProperties.getFrontendHost() + "/reset-password?token=" + rawToken;

        EmployeeModel emp = employeeRepository.findByUserId(user.getId()).orElse(null);
        String firstName = emp != null ? emp.getFirst_name() : user.getUsername();

        mailService.sendHtmlEmail(
                user.getEmail(),
                "Recuperación de contraseña",
                "email/password-reset",
                Map.of(
                        "firstName", firstName,
                        "username", user.getUsername(),
                        "resetLink", resetLink,
                        "expirationHours", RESET_TOKEN_EXPIRATION_HOURS
                )
        );
    }

    /**
     * Validates a password reset token and updates the user's password.
     *
     * @param dto request containing the token and new password
     * @throws InvalidTokenException if the token is invalid, expired, or the passwords don't match
     */
    public void resetPassword(ResetPasswordDTO dto) throws InvalidTokenException {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidTokenException("Las contraseñas no coinciden.");
        }

        PasswordResetTokenModel tokenModel = passwordResetTokenRepository
                .findByTokenAndUsedFalse(dto.getToken())
                .orElseThrow(() -> new InvalidTokenException(
                        "El token de recuperación es inválido o ya fue utilizado."));

        if (tokenModel.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("El token de recuperación ha expirado.");
        }

        UserModel user = tokenModel.getUser();
        user.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        // Mark token as used
        tokenModel.setUsed(true);
        passwordResetTokenRepository.save(tokenModel);

        // Send confirmation email
        EmployeeModel emp = employeeRepository.findByUserId(user.getId()).orElse(null);
        String firstName = emp != null ? emp.getFirst_name() : user.getUsername();

        mailService.sendHtmlEmail(
                user.getEmail(),
                "Tu contraseña ha sido restablecida",
                "email/password-set-confirmation",
                Map.of(
                        "firstName", firstName,
                        "username", user.getUsername()
                )
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
