package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.services.RoleService;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.SetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.OnboardingTokenModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.OnboardingTokenRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import edu.robertob.ayd2_p1_backend.core.config.AppProperties;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.DuplicateResourceException;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.services.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock UserRepository userRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock OnboardingTokenRepository onboardingTokenRepository;
    @Mock RoleService roleService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserMapper userMapper;
    @Mock MailService mailService;
    @Mock AppProperties appProperties;

    @InjectMocks
    UserManagementService service;

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_throwsOnDuplicateUsername() {
        CreateUserDTO dto = mockCreateDto("juan", "a@mail.com", "pass1234");
        when(userRepository.existsByUsername("juan")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwsOnDuplicateEmail() {
        CreateUserDTO dto = mockCreateDto("juan", "a@mail.com", "pass1234");
        when(userRepository.existsByUsername("juan")).thenReturn(false);
        when(userRepository.existsByEmail("a@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.createUser(dto));
    }

    @Test
    void createUser_successWithPasswordSkipsEmail() throws NotFoundException {
        CreateUserDTO dto = mockCreateDto("juan", "a@mail.com", "pass1234");
        UserModel saved = buildUser(1L, "juan", "a@mail.com");
        EmployeeModel savedEmp = buildEmployee(1L, saved);
        UserDTO expected = buildUserDTO();

        when(userRepository.existsByUsername("juan")).thenReturn(false);
        when(userRepository.existsByEmail("a@mail.com")).thenReturn(false);
        when(roleService.findRoleById(1L)).thenReturn(buildRole());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(saved);
        when(employeeRepository.save(any())).thenReturn(savedEmp);
        when(userMapper.userToUserDTO(saved, savedEmp)).thenReturn(expected);

        assertSame(expected, service.createUser(dto));
        verify(mailService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void createUser_successWithoutPasswordSendsEmail() throws NotFoundException {
        CreateUserDTO dto = mockCreateDto("bob", "b@mail.com", null);
        UserModel saved = buildUser(2L, "bob", "b@mail.com");
        EmployeeModel savedEmp = buildEmployee(2L, saved);

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("b@mail.com")).thenReturn(false);
        when(roleService.findRoleById(1L)).thenReturn(buildRole());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(saved);
        when(employeeRepository.save(any())).thenReturn(savedEmp);
        when(onboardingTokenRepository.save(any())).thenReturn(new OnboardingTokenModel());
        when(appProperties.getFrontendHost()).thenReturn("http://localhost:3000");
        when(userMapper.userToUserDTO(any(), any())).thenReturn(buildUserDTO());

        service.createUser(dto);

        verify(mailService).sendHtmlEmail(eq("b@mail.com"), any(), any(), any());
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_returnsUserWhenFound() throws NotFoundException {
        UserModel user = buildUser(1L, "juan", "a@mail.com");
        EmployeeModel emp = buildEmployee(1L, user);
        UserDTO expected = buildUserDTO();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(emp));
        when(userMapper.userToUserDTO(user, emp)).thenReturn(expected);

        assertSame(expected, service.getUserById(1L));
    }

    @Test
    void getUserById_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getUserById(99L));
    }

    // ── toggleUserStatus ──────────────────────────────────────────────────────

    @Test
    void toggleUserStatus_throwsWhenTogglingOwnAccount() {
        UserModel auth = buildUser(1L, "juan", "a@mail.com");
        when(userRepository.findUserByUsername("juan")).thenReturn(Optional.of(auth));

        assertThrows(BadRequestException.class, () -> service.toggleUserStatus(1L, "juan"));
    }

    @Test
    void toggleUserStatus_throwsWhenOnboardingPending() {
        UserModel auth = buildUser(1L, "admin", "admin@mail.com");
        UserModel target = buildUser(2L, "bob", "b@mail.com");
        target.setActive(false);

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(auth));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(onboardingTokenRepository.existsByUserIdAndUsedFalse(2L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.toggleUserStatus(2L, "admin"));
    }

    @Test
    void toggleUserStatus_deactivatesActiveUser() throws NotFoundException {
        UserModel auth = buildUser(1L, "admin", "admin@mail.com");
        UserModel target = buildUser(2L, "bob", "b@mail.com");

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(auth));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(userMapper.userToUserDTO(any(), any())).thenReturn(buildUserDTO());

        service.toggleUserStatus(2L, "admin");

        assertFalse(target.isActive());
    }

    // ── resendOnboardingEmail ─────────────────────────────────────────────────

    @Test
    void resendOnboardingEmail_throwsWhenAlreadyOnboarded() {
        UserModel user = buildUser(1L, "juan", "a@mail.com");
        user.setOnboardingCompleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> service.resendOnboardingEmail(1L));
        verify(mailService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void resendOnboardingEmail_deletesOldTokenAndSendsEmail() throws NotFoundException {
        UserModel user = buildUser(1L, "juan", "a@mail.com");
        user.setOnboardingCompleted(false);
        EmployeeModel emp = buildEmployee(1L, user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(emp));
        when(onboardingTokenRepository.save(any())).thenReturn(new OnboardingTokenModel());
        when(appProperties.getFrontendHost()).thenReturn("http://localhost:3000");

        service.resendOnboardingEmail(1L);

        verify(onboardingTokenRepository).deleteByUserId(1L);
        verify(mailService).sendHtmlEmail(eq("a@mail.com"), any(), any(), any());
    }

    // ── setPasswordFromOnboarding ─────────────────────────────────────────────

    @Test
    void setPasswordFromOnboarding_throwsOnPasswordMismatch() {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("abc12345");
        when(dto.getConfirmPassword()).thenReturn("different");

        assertThrows(InvalidTokenException.class, () -> service.setPasswordFromOnboarding(dto));
    }

    @Test
    void setPasswordFromOnboarding_throwsOnInvalidToken() {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("abc12345");
        when(dto.getConfirmPassword()).thenReturn("abc12345");
        when(dto.getToken()).thenReturn("bad-token");
        when(onboardingTokenRepository.findByTokenAndUsedFalse("bad-token")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> service.setPasswordFromOnboarding(dto));
    }

    @Test
    void setPasswordFromOnboarding_throwsOnExpiredToken() {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("abc12345");
        when(dto.getConfirmPassword()).thenReturn("abc12345");
        when(dto.getToken()).thenReturn("expired-token");

        OnboardingTokenModel token = new OnboardingTokenModel();
        token.setUser(buildUser(1L, "juan", "a@mail.com"));
        token.setExpiresAt(Instant.now().minusSeconds(3600));
        when(onboardingTokenRepository.findByTokenAndUsedFalse("expired-token")).thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> service.setPasswordFromOnboarding(dto));
    }

    @Test
    void setPasswordFromOnboarding_activatesUserAndSendsConfirmation() {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("abc12345");
        when(dto.getConfirmPassword()).thenReturn("abc12345");
        when(dto.getToken()).thenReturn("valid-token");

        UserModel user = buildUser(1L, "juan", "a@mail.com");
        OnboardingTokenModel token = new OnboardingTokenModel();
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        when(onboardingTokenRepository.findByTokenAndUsedFalse("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("abc12345")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        when(onboardingTokenRepository.save(token)).thenReturn(token);
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.setPasswordFromOnboarding(dto));
        assertTrue(user.isActive());
        assertTrue(user.isOnboardingCompleted());
        assertTrue(token.isUsed());
        verify(mailService).sendHtmlEmail(eq("a@mail.com"), any(), any(), any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CreateUserDTO mockCreateDto(String username, String email, String password) {
        CreateUserDTO dto = mock(CreateUserDTO.class);
        CreateUserDTO.EmployeeDataDTO empDto = mock(CreateUserDTO.EmployeeDataDTO.class);
        when(dto.getUsername()).thenReturn(username);
        lenient().when(dto.getEmail()).thenReturn(email);
        lenient().when(dto.getPassword()).thenReturn(password);
        lenient().when(dto.getRoleId()).thenReturn(1L);
        lenient().when(dto.getEmployee()).thenReturn(empDto);
        lenient().when(empDto.getFirstName()).thenReturn("First");
        lenient().when(empDto.getLastName()).thenReturn("Last");
        lenient().when(empDto.getHourlyRate()).thenReturn(20.0);
        return dto;
    }

    private UserModel buildUser(Long id, String username, String email) {
        UserModel user = new UserModel();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(true);
        user.setOnboardingCompleted(true);
        user.setRole(buildRole());
        user.setPassword_hash("hashed");
        return user;
    }

    private EmployeeModel buildEmployee(Long id, UserModel user) {
        EmployeeModel emp = new EmployeeModel();
        emp.setId(id);
        emp.setUser(user);
        emp.setFirst_name("First");
        emp.setLast_name("Last");
        emp.setHourly_rate(20.0);
        return emp;
    }

    private RoleModel buildRole() {
        RoleModel role = new RoleModel();
        role.setId(1L);
        role.setCode(RolesEnum.DEVELOPER);
        role.setName("Developer");
        return role;
    }

    private UserDTO buildUserDTO() {
        return new UserDTO(1L, "juan", "a@mail.com", true, true,
                new UserDTO.RoleInfoDTO(1L, "DEVELOPER", "Developer"),
                new UserDTO.EmployeeDTO(1L, "First", "Last", 20.0));
    }
}
