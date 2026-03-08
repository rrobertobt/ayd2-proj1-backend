package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.services.RoleService;
import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.SetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UpdateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UserFilterDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.OnboardingTokenModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.OnboardingTokenRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.core.config.AppProperties;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.DuplicateResourceException;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import edu.robertob.ayd2_p1_backend.core.services.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private OnboardingTokenRepository onboardingTokenRepository;
    @Mock private RoleService roleService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private MailService mailService;
    @Mock private AppProperties appProperties;

    @InjectMocks
    private UserManagementService userManagementService;

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_withPassword_savesUserAndEmployee() throws NotFoundException {
        CreateUserDTO dto = buildCreateUserDTO("alice", "alice@mail.com", 1L, "pass1234");
        RoleModel role = buildRole(1L, RolesEnum.DEVELOPER, "Developer");
        UserModel savedUser = buildUser(10L, "alice", "alice@mail.com", role);
        EmployeeModel savedEmployee = buildEmployee(5L, savedUser);
        UserDTO expected = buildUserDTO(savedUser, savedEmployee);

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(roleService.findRoleById(1L)).thenReturn(role);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(employeeRepository.save(any(EmployeeModel.class))).thenReturn(savedEmployee);
        when(userMapper.userToUserDTO(savedUser, savedEmployee)).thenReturn(expected);

        UserDTO result = userManagementService.createUser(dto);

        assertSame(expected, result);
        // Verify the user was saved with active=true
        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isActive());
        assertTrue(userCaptor.getValue().isOnboardingCompleted());
        verify(employeeRepository).save(any(EmployeeModel.class));
        verify(mailService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void createUser_withoutPassword_savesAndSendsOnboardingEmail() throws NotFoundException {
        CreateUserDTO dto = buildCreateUserDTO("bob", "bob@mail.com", 2L, null);
        RoleModel role = buildRole(2L, RolesEnum.PROJECT_ADMIN, "Project Admin");
        UserModel savedUser = buildUser(11L, "bob", "bob@mail.com", role);
        EmployeeModel savedEmployee = buildEmployee(6L, savedUser);
        UserDTO expected = buildUserDTO(savedUser, savedEmployee);

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@mail.com")).thenReturn(false);
        when(roleService.findRoleById(2L)).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("placeholder_hash");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(employeeRepository.save(any(EmployeeModel.class))).thenReturn(savedEmployee);
        when(onboardingTokenRepository.save(any(OnboardingTokenModel.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(appProperties.getFrontendHost()).thenReturn("http://localhost:3000");
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), any());
        when(userMapper.userToUserDTO(savedUser, savedEmployee)).thenReturn(expected);

        UserDTO result = userManagementService.createUser(dto);

        assertSame(expected, result);
        // Verify the user was saved with active=false (inactive until onboarding is completed)
        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isActive());
        assertFalse(userCaptor.getValue().isOnboardingCompleted());
        verify(onboardingTokenRepository).save(any(OnboardingTokenModel.class));
        verify(mailService).sendHtmlEmail(eq("bob@mail.com"), any(), eq("email/onboarding-invitation"), any());
    }

    @Test
    void createUser_duplicateUsername_throwsDuplicateResourceException() {
        CreateUserDTO dto = buildCreateUserDTO("existing", "new@mail.com", 1L, null);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userManagementService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_duplicateEmail_throwsDuplicateResourceException() {
        CreateUserDTO dto = buildCreateUserDTO("newuser", "existing@mail.com", 1L, null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userManagementService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_roleNotFound_throwsNotFoundException() throws NotFoundException {
        CreateUserDTO dto = buildCreateUserDTO("carol", "carol@mail.com", 99L, null);
        when(userRepository.existsByUsername("carol")).thenReturn(false);
        when(userRepository.existsByEmail("carol@mail.com")).thenReturn(false);
        when(roleService.findRoleById(99L)).thenThrow(new NotFoundException("Role not found"));

        assertThrows(NotFoundException.class, () -> userManagementService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    // ── getUsers ──────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getUsers_returnsPagedResponse() {
        UserFilterDTO filter = new UserFilterDTO();
        UserModel user = buildUser(1L, "alice", "alice@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        EmployeeModel emp = buildEmployee(1L, user);
        UserDTO userDTO = buildUserDTO(user, emp);

        Page<UserModel> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(emp));
        when(userMapper.userToUserDTO(user, emp)).thenReturn(userDTO);

        PagedResponseDTO<UserDTO> result = userManagementService.getUsers(filter);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertSame(userDTO, result.content().get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUsers_withEmployeeSort_usesSortUnsorted() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setSortBy("firstName");
        Page<UserModel> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        PagedResponseDTO<UserDTO> result = userManagementService.getUsers(filter);

        assertNotNull(result);
        assertEquals(0, result.content().size());
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_found_returnsUserDTO() throws NotFoundException {
        UserModel user = buildUser(5L, "dave", "dave@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        EmployeeModel emp = buildEmployee(3L, user);
        UserDTO expected = buildUserDTO(user, emp);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(5L)).thenReturn(Optional.of(emp));
        when(userMapper.userToUserDTO(user, emp)).thenReturn(expected);

        UserDTO result = userManagementService.getUserById(5L);

        assertSame(expected, result);
    }

    @Test
    void getUserById_notFound_throwsNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userManagementService.getUserById(999L));
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_notFound_throwsNotFoundException() {
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userManagementService.updateUser(404L, dto));
    }

    @Test
    void updateUser_changesUsername_savesUser() throws NotFoundException {
        UserModel user = buildUser(1L, "old", "user@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        when(dto.getUsername()).thenReturn("newname");
        when(dto.getEmail()).thenReturn(null);
        when(dto.getRoleId()).thenReturn(null);
        when(dto.getEmployee()).thenReturn(null);
        UserDTO expected = buildUserDTO(user, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userMapper.userToUserDTO(user, null)).thenReturn(expected);

        UserDTO result = userManagementService.updateUser(1L, dto);

        assertSame(expected, result);
        assertEquals("newname", user.getUsername());
    }

    @Test
    void updateUser_duplicateUsername_throwsDuplicateResourceException() {
        UserModel user = buildUser(1L, "old", "user@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        when(dto.getUsername()).thenReturn("taken");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userManagementService.updateUser(1L, dto));
    }

    @Test
    void updateUser_changesEmail_savesUser() throws NotFoundException {
        UserModel user = buildUser(2L, "user2", "old@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        when(dto.getUsername()).thenReturn(null);
        when(dto.getEmail()).thenReturn("new@mail.com");
        when(dto.getRoleId()).thenReturn(null);
        when(dto.getEmployee()).thenReturn(null);
        UserDTO expected = buildUserDTO(user, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(userMapper.userToUserDTO(user, null)).thenReturn(expected);

        UserDTO result = userManagementService.updateUser(2L, dto);

        assertSame(expected, result);
        assertEquals("new@mail.com", user.getEmail());
    }

    @Test
    void updateUser_duplicateEmail_throwsDuplicateResourceException() {
        UserModel user = buildUser(2L, "user2", "old@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        when(dto.getUsername()).thenReturn(null);
        when(dto.getEmail()).thenReturn("taken@mail.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userManagementService.updateUser(2L, dto));
    }

    @Test
    void updateUser_changesRole_savesUser() throws NotFoundException {
        UserModel user = buildUser(3L, "user3", "user3@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        RoleModel newRole = buildRole(2L, RolesEnum.PROJECT_ADMIN, "PA");
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        when(dto.getUsername()).thenReturn(null);
        when(dto.getEmail()).thenReturn(null);
        when(dto.getRoleId()).thenReturn(2L);
        when(dto.getEmployee()).thenReturn(null);
        UserDTO expected = buildUserDTO(user, null);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(roleService.findRoleById(2L)).thenReturn(newRole);
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.findByUserId(3L)).thenReturn(Optional.empty());
        when(userMapper.userToUserDTO(user, null)).thenReturn(expected);

        UserDTO result = userManagementService.updateUser(3L, dto);

        assertSame(expected, result);
        assertSame(newRole, user.getRole());
    }

    @Test
    void updateUser_withEmployeeData_updatesEmployee() throws NotFoundException {
        UserModel user = buildUser(4L, "user4", "user4@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        EmployeeModel emp = buildEmployee(7L, user);
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        UpdateUserDTO.EmployeeUpdateDataDTO empDto = mock(UpdateUserDTO.EmployeeUpdateDataDTO.class);
        when(empDto.getFirstName()).thenReturn("NewFirst");
        when(empDto.getLastName()).thenReturn("NewLast");
        when(empDto.getHourlyRate()).thenReturn(30.0);
        when(dto.getUsername()).thenReturn(null);
        when(dto.getEmail()).thenReturn(null);
        when(dto.getRoleId()).thenReturn(null);
        when(dto.getEmployee()).thenReturn(empDto);
        UserDTO expected = buildUserDTO(user, emp);

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.findByUserId(4L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(emp)).thenReturn(emp);
        when(userMapper.userToUserDTO(user, emp)).thenReturn(expected);

        UserDTO result = userManagementService.updateUser(4L, dto);

        assertSame(expected, result);
        assertEquals("NewFirst", emp.getFirst_name());
        assertEquals("NewLast", emp.getLast_name());
        assertEquals(30.0, emp.getHourly_rate());
    }

    // ── toggleUserStatus ──────────────────────────────────────────────────────

    @Test
    void toggleUserStatus_activeToInactive() throws NotFoundException {
        UserModel admin = buildUser(99L, "admin", "admin@mail.com", buildRole(1L, RolesEnum.SYSTEM_ADMIN, "Admin"));
        UserModel user = buildUser(1L, "alice", "alice@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        user.setActive(true);
        UserDTO expected = buildUserDTO(user, null);

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // active→inactive: no need to check pending tokens
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userMapper.userToUserDTO(user, null)).thenReturn(expected);

        UserDTO result = userManagementService.toggleUserStatus(1L, "admin");

        assertSame(expected, result);
        assertFalse(user.isActive());
    }

    @Test
    void toggleUserStatus_inactiveToActive_noPendingToken_succeeds() throws NotFoundException {
        UserModel admin = buildUser(99L, "admin", "admin@mail.com", buildRole(1L, RolesEnum.SYSTEM_ADMIN, "Admin"));
        UserModel user = buildUser(2L, "bob", "bob@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        user.setActive(false);
        UserDTO expected = buildUserDTO(user, null);

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(onboardingTokenRepository.existsByUserIdAndUsedFalse(2L)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(userMapper.userToUserDTO(user, null)).thenReturn(expected);

        userManagementService.toggleUserStatus(2L, "admin");

        assertTrue(user.isActive());
    }

    @Test
    void toggleUserStatus_inactiveToActive_pendingOnboardingToken_throwsBadRequestException() {
        UserModel admin = buildUser(99L, "admin", "admin@mail.com", buildRole(1L, RolesEnum.SYSTEM_ADMIN, "Admin"));
        UserModel user = buildUser(3L, "carol", "carol@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        user.setActive(false);

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(onboardingTokenRepository.existsByUserIdAndUsedFalse(3L)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userManagementService.toggleUserStatus(3L, "admin"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleUserStatus_selfToggle_throwsBadRequestException() {
        UserModel admin = buildUser(99L, "admin", "admin@mail.com", buildRole(1L, RolesEnum.SYSTEM_ADMIN, "Admin"));

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class,
                () -> userManagementService.toggleUserStatus(99L, "admin"));
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleUserStatus_notFound_throwsNotFoundException() {
        UserModel admin = buildUser(99L, "admin", "admin@mail.com", buildRole(1L, RolesEnum.SYSTEM_ADMIN, "Admin"));

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userManagementService.toggleUserStatus(999L, "admin"));
    }

    // ── resendOnboardingEmail ─────────────────────────────────────────────────

    @Test
    void resendOnboardingEmail_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userManagementService.resendOnboardingEmail(999L));
        verifyNoInteractions(onboardingTokenRepository);
    }

    @Test
    void resendOnboardingEmail_userAlreadyCompletedOnboarding_throwsBadRequestException() {
        UserModel user = buildUser(1L, "alice", "alice@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        user.setOnboardingCompleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> userManagementService.resendOnboardingEmail(1L));
        verify(onboardingTokenRepository, never()).deleteByUserId(any());
        verify(mailService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void resendOnboardingEmail_inactiveUser_deletesOldTokensAndSendsEmail() throws NotFoundException {
        UserModel user = buildUser(2L, "bob", "bob@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        user.setActive(false);
        EmployeeModel emp = buildEmployee(3L, user);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        doNothing().when(onboardingTokenRepository).deleteByUserId(2L);
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.of(emp));
        when(onboardingTokenRepository.save(any(OnboardingTokenModel.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(appProperties.getFrontendHost()).thenReturn("http://localhost:3000");
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), any());

        userManagementService.resendOnboardingEmail(2L);

        verify(onboardingTokenRepository).deleteByUserId(2L);
        verify(onboardingTokenRepository).save(any(OnboardingTokenModel.class));
        verify(mailService).sendHtmlEmail(
                eq("bob@mail.com"), any(), eq("email/onboarding-invitation"), any());
    }

    @Test
    void resendOnboardingEmail_inactiveUserNoEmployee_usesUsernameAsFirstName() throws NotFoundException {
        UserModel user = buildUser(3L, "charlie", "charlie@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        user.setActive(false);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        doNothing().when(onboardingTokenRepository).deleteByUserId(3L);
        when(employeeRepository.findByUserId(3L)).thenReturn(Optional.empty());
        when(onboardingTokenRepository.save(any(OnboardingTokenModel.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(appProperties.getFrontendHost()).thenReturn("http://localhost:3000");

        ArgumentCaptor<java.util.Map> mapCaptor = ArgumentCaptor.forClass(java.util.Map.class);
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), mapCaptor.capture());

        userManagementService.resendOnboardingEmail(3L);

        java.util.Map<?, ?> templateVars = mapCaptor.getValue();
        assertEquals("charlie", templateVars.get("firstName"));
    }

    // ── setPasswordFromOnboarding ─────────────────────────────────────────────

    @Test
    void setPasswordFromOnboarding_passwordMismatch_throwsInvalidTokenException() {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("pass1");
        when(dto.getConfirmPassword()).thenReturn("pass2");

        assertThrows(InvalidTokenException.class,
                () -> userManagementService.setPasswordFromOnboarding(dto));
        verifyNoInteractions(onboardingTokenRepository);
    }

    @Test
    void setPasswordFromOnboarding_tokenNotFound_throwsInvalidTokenException() {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("newpass1");
        when(dto.getConfirmPassword()).thenReturn("newpass1");
        when(dto.getToken()).thenReturn("badtoken");
        when(onboardingTokenRepository.findByTokenAndUsedFalse("badtoken"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class,
                () -> userManagementService.setPasswordFromOnboarding(dto));
    }

    @Test
    void setPasswordFromOnboarding_tokenExpired_throwsInvalidTokenException() {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("newpass1");
        when(dto.getConfirmPassword()).thenReturn("newpass1");
        when(dto.getToken()).thenReturn("expiredtoken");

        OnboardingTokenModel tokenModel = new OnboardingTokenModel();
        tokenModel.setExpiresAt(Instant.now().minusSeconds(3600)); // expired 1 hour ago
        when(onboardingTokenRepository.findByTokenAndUsedFalse("expiredtoken"))
                .thenReturn(Optional.of(tokenModel));

        assertThrows(InvalidTokenException.class,
                () -> userManagementService.setPasswordFromOnboarding(dto));
    }

    @Test
    void setPasswordFromOnboarding_valid_setsPasswordAndSendsConfirmation() throws InvalidTokenException {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("newpass1");
        when(dto.getConfirmPassword()).thenReturn("newpass1");
        when(dto.getToken()).thenReturn("validtoken");

        UserModel user = buildUser(1L, "alice", "alice@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));
        EmployeeModel emp = buildEmployee(1L, user);

        OnboardingTokenModel tokenModel = new OnboardingTokenModel();
        tokenModel.setUser(user);
        tokenModel.setToken("validtoken");
        tokenModel.setUsed(false);
        tokenModel.setExpiresAt(Instant.now().plusSeconds(3600)); // expires in 1 hour

        when(onboardingTokenRepository.findByTokenAndUsedFalse("validtoken"))
                .thenReturn(Optional.of(tokenModel));
        when(passwordEncoder.encode("newpass1")).thenReturn("newhash");
        when(userRepository.save(user)).thenReturn(user);
        when(onboardingTokenRepository.save(tokenModel)).thenReturn(tokenModel);
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(emp));
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), any());

        userManagementService.setPasswordFromOnboarding(dto);

        assertEquals("newhash", user.getPassword_hash());
        assertTrue(user.isActive());
        assertTrue(user.isOnboardingCompleted());
        assertTrue(tokenModel.isUsed());
        verify(mailService).sendHtmlEmail(
                eq("alice@mail.com"), any(), eq("email/password-set-confirmation"), any());
    }

    @Test
    void setPasswordFromOnboarding_validNoEmployee_usesUsername() throws InvalidTokenException {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        when(dto.getPassword()).thenReturn("newpass2");
        when(dto.getConfirmPassword()).thenReturn("newpass2");
        when(dto.getToken()).thenReturn("validtoken2");

        UserModel user = buildUser(2L, "charlie", "charlie@mail.com", buildRole(1L, RolesEnum.DEVELOPER, "Dev"));

        OnboardingTokenModel tokenModel = new OnboardingTokenModel();
        tokenModel.setUser(user);
        tokenModel.setToken("validtoken2");
        tokenModel.setUsed(false);
        tokenModel.setExpiresAt(Instant.now().plusSeconds(3600));

        when(onboardingTokenRepository.findByTokenAndUsedFalse("validtoken2"))
                .thenReturn(Optional.of(tokenModel));
        when(passwordEncoder.encode("newpass2")).thenReturn("newhash2");
        when(userRepository.save(user)).thenReturn(user);
        when(onboardingTokenRepository.save(tokenModel)).thenReturn(tokenModel);
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.empty());

        ArgumentCaptor<java.util.Map> mapCaptor = ArgumentCaptor.forClass(java.util.Map.class);
        doNothing().when(mailService).sendHtmlEmail(any(), any(), any(), mapCaptor.capture());

        userManagementService.setPasswordFromOnboarding(dto);

        // When no employee, firstName falls back to username
        java.util.Map<?, ?> templateVars = mapCaptor.getValue();
        assertEquals("charlie", templateVars.get("firstName"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CreateUserDTO buildCreateUserDTO(String username, String email, Long roleId, String password) {
        CreateUserDTO dto = mock(CreateUserDTO.class);
        lenient().when(dto.getUsername()).thenReturn(username);
        lenient().when(dto.getEmail()).thenReturn(email);
        lenient().when(dto.getRoleId()).thenReturn(roleId);
        lenient().when(dto.getPassword()).thenReturn(password);
        CreateUserDTO.EmployeeDataDTO empDto = mock(CreateUserDTO.EmployeeDataDTO.class);
        lenient().when(empDto.getFirstName()).thenReturn("First");
        lenient().when(empDto.getLastName()).thenReturn("Last");
        lenient().when(empDto.getHourlyRate()).thenReturn(20.0);
        lenient().when(dto.getEmployee()).thenReturn(empDto);
        return dto;
    }

    private UserModel buildUser(Long id, String username, String email, RoleModel role) {
        UserModel user = new UserModel();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(true);
        user.setRole(role);
        user.setPassword_hash("hashed");
        return user;
    }

    private EmployeeModel buildEmployee(Long id, UserModel user) {
        EmployeeModel emp = new EmployeeModel();
        emp.setId(id);
        emp.setUser(user);
        emp.setFirst_name("First");
        emp.setLast_name("Last");
        emp.setHourly_rate(25.0);
        return emp;
    }

    private RoleModel buildRole(Long id, RolesEnum code, String name) {
        RoleModel role = new RoleModel();
        role.setId(id);
        role.setCode(code);
        role.setName(name);
        role.setDescription(name + " role");
        return role;
    }

    private UserDTO buildUserDTO(UserModel user, EmployeeModel emp) {
        UserDTO.RoleInfoDTO roleDTO = new UserDTO.RoleInfoDTO(
                user.getRole().getId(), user.getRole().getCode().name(), user.getRole().getName());
        UserDTO.EmployeeDTO employeeDTO = emp == null ? null :
                new UserDTO.EmployeeDTO(emp.getId(), emp.getFirst_name(), emp.getLast_name(), emp.getHourly_rate());
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.isActive(), user.isOnboardingCompleted(), roleDTO, employeeDTO);
    }
}
