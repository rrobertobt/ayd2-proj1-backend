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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MailService mailService;
    @Mock private AppProperties appProperties;

    @InjectMocks
    private PasswordResetService passwordResetService;

    // ── requestPasswordReset ──────────────────────────────────────────────────

    @Test
    void requestPasswordReset_unknownEmail_returnsWithoutAction() {
        ForgotPasswordDTO dto = buildForgotPasswordDTO("unknown@mail.com");
        when(userRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset(dto);

        verify(passwordResetTokenRepository, never()).deleteByUserId(any());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(mailService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void requestPasswordReset_inactiveUser_returnsWithoutAction() {
        ForgotPasswordDTO dto = buildForgotPasswordDTO("inactive@mail.com");
        UserModel inactiveUser = buildUser(1L, "inactive", "inactive@mail.com", false);
        when(userRepository.findByEmail("inactive@mail.com")).thenReturn(Optional.of(inactiveUser));

        passwordResetService.requestPasswordReset(dto);

        verify(passwordResetTokenRepository, never()).deleteByUserId(any());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(mailService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void requestPasswordReset_activeUser_deletesOldTokensAndSendsEmail() {
        ForgotPasswordDTO dto = buildForgotPasswordDTO("user@mail.com");
        UserModel user = buildUser(2L, "alice", "user@mail.com", true);
        EmployeeModel emp = buildEmployee(user, "Alice");

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        doNothing().when(passwordResetTokenRepository).deleteByUserId(2L);
        when(passwordResetTokenRepository.save(any(PasswordResetTokenModel.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.of(emp));
        when(appProperties.getFrontendHost()).thenReturn("http://localhost:3000");
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), any());

        passwordResetService.requestPasswordReset(dto);

        verify(passwordResetTokenRepository).deleteByUserId(2L);

        ArgumentCaptor<PasswordResetTokenModel> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetTokenModel.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetTokenModel savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getToken());
        assertFalse(savedToken.isUsed());
        assertTrue(savedToken.getExpiresAt().isAfter(Instant.now()));

        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendHtmlEmail(
                eq("user@mail.com"),
                eq("Recuperación de contraseña"),
                eq("email/password-reset"),
                varsCaptor.capture()
        );
        Map<String, Object> vars = varsCaptor.getValue();
        assertEquals("Alice", vars.get("firstName"));
        assertEquals("alice", vars.get("username"));
        assertTrue(vars.get("resetLink").toString().startsWith("http://localhost:3000/reset-password?token="));
    }

    @Test
    void requestPasswordReset_activeUserNoEmployee_usesUsernameAsFirstName() {
        ForgotPasswordDTO dto = buildForgotPasswordDTO("user@mail.com");
        UserModel user = buildUser(3L, "bob", "user@mail.com", true);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        doNothing().when(passwordResetTokenRepository).deleteByUserId(3L);
        when(passwordResetTokenRepository.save(any(PasswordResetTokenModel.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.findByUserId(3L)).thenReturn(Optional.empty());
        when(appProperties.getFrontendHost()).thenReturn("http://localhost:3000");
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), any());

        passwordResetService.requestPasswordReset(dto);

        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendHtmlEmail(any(), any(), any(), varsCaptor.capture());
        assertEquals("bob", varsCaptor.getValue().get("firstName"));
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_passwordMismatch_throwsInvalidTokenException() {
        ResetPasswordDTO dto = buildResetPasswordDTO("tok123", "pass1234", "different");

        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword(dto));
        verify(passwordResetTokenRepository, never()).findByTokenAndUsedFalse(any());
    }

    @Test
    void resetPassword_invalidToken_throwsInvalidTokenException() {
        ResetPasswordDTO dto = buildResetPasswordDTO("badtoken", "pass1234", "pass1234");
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("badtoken"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_expiredToken_throwsInvalidTokenException() {
        ResetPasswordDTO dto = buildResetPasswordDTO("expiredtoken", "pass1234", "pass1234");
        UserModel user = buildUser(4L, "carol", "carol@mail.com", true);
        PasswordResetTokenModel token = buildToken(user, "expiredtoken",
                Instant.now().minusSeconds(3600));

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("expiredtoken"))
                .thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_validToken_updatesPasswordAndSendsConfirmation() throws InvalidTokenException {
        ResetPasswordDTO dto = buildResetPasswordDTO("validtoken", "newpass1234", "newpass1234");
        UserModel user = buildUser(5L, "dave", "dave@mail.com", true);
        EmployeeModel emp = buildEmployee(user, "Dave");
        PasswordResetTokenModel token = buildToken(user, "validtoken",
                Instant.now().plusSeconds(3600));

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("validtoken"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass1234")).thenReturn("hashed_new");
        when(userRepository.save(user)).thenReturn(user);
        when(passwordResetTokenRepository.save(token)).thenReturn(token);
        when(employeeRepository.findByUserId(5L)).thenReturn(Optional.of(emp));
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), any());

        passwordResetService.resetPassword(dto);

        assertEquals("hashed_new", user.getPassword_hash());
        assertTrue(token.isUsed());

        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
        verify(mailService).sendHtmlEmail(
                eq("dave@mail.com"),
                eq("Tu contraseña ha sido restablecida"),
                eq("email/password-set-confirmation"),
                argThat(vars -> "Dave".equals(vars.get("firstName")) && "dave".equals(vars.get("username")))
        );
    }

    @Test
    void resetPassword_noEmployee_usesUsernameAsFirstName() throws InvalidTokenException {
        ResetPasswordDTO dto = buildResetPasswordDTO("tok", "pass1234", "pass1234");
        UserModel user = buildUser(6L, "eve", "eve@mail.com", true);
        PasswordResetTokenModel token = buildToken(user, "tok", Instant.now().plusSeconds(3600));

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("tok"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        when(passwordResetTokenRepository.save(token)).thenReturn(token);
        when(employeeRepository.findByUserId(6L)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), any());

        passwordResetService.resetPassword(dto);

        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendHtmlEmail(any(), any(), any(), varsCaptor.capture());
        assertEquals("eve", varsCaptor.getValue().get("firstName"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ForgotPasswordDTO buildForgotPasswordDTO(String email) {
        ForgotPasswordDTO dto = mock(ForgotPasswordDTO.class);
        when(dto.getEmail()).thenReturn(email);
        return dto;
    }

    private ResetPasswordDTO buildResetPasswordDTO(String token, String password, String confirm) {
        ResetPasswordDTO dto = mock(ResetPasswordDTO.class);
        when(dto.getPassword()).thenReturn(password);
        when(dto.getConfirmPassword()).thenReturn(confirm);
        if (password.equals(confirm)) {
            when(dto.getToken()).thenReturn(token);
        }
        return dto;
    }

    private UserModel buildUser(Long id, String username, String email, boolean active) {
        UserModel user = new UserModel();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(active);
        return user;
    }

    private EmployeeModel buildEmployee(UserModel user, String firstName) {
        EmployeeModel emp = new EmployeeModel();
        emp.setId(10L);
        emp.setFirst_name(firstName);
        emp.setUser(user);
        return emp;
    }

    private PasswordResetTokenModel buildToken(UserModel user, String token, Instant expiresAt) {
        PasswordResetTokenModel t = new PasswordResetTokenModel();
        t.setUser(user);
        t.setToken(token);
        t.setUsed(false);
        t.setExpiresAt(expiresAt);
        return t;
    }
}
