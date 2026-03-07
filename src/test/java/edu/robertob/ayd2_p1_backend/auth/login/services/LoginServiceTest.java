package edu.robertob.ayd2_p1_backend.auth.login.services;

import edu.robertob.ayd2_p1_backend.auth.jwt.services.JwtGeneratorService;
import edu.robertob.ayd2_p1_backend.auth.login.models.dto.request.LoginDTO;
import edu.robertob.ayd2_p1_backend.auth.login.models.dto.response.LoginResponseDTO;
import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtGeneratorService jwtGeneratorService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LoginService loginService;

    @Test
    void login_shouldReturnTokenAndUserInfoWhenCredentialsAreValid() throws NotFoundException {
        LoginDTO loginDTO = new LoginDTO("john", "raw-pass");
        UserModel user = buildUser(1L, "john", "john@mail.com", true);
        EmployeeModel employee = buildEmployee(99L, user);

        UserDTO.RoleInfoDTO roleDTO = new UserDTO.RoleInfoDTO(7L, "SYSTEM_ADMIN", "Administrador");
        UserDTO.EmployeeDTO empDTO = new UserDTO.EmployeeDTO(99L, "John", "Doe", 50.0);
        UserDTO userDTO = new UserDTO(1L, "john", "john@mail.com", true, roleDTO, empDTO);

        when(userService.getUserByUsername("john")).thenReturn(user);
        when(passwordEncoder.matches("raw-pass", "hashed")).thenReturn(true);
        when(jwtGeneratorService.generateToken(user)).thenReturn("jwt-token");
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(employee));
        when(userMapper.userToUserDTO(user, employee)).thenReturn(userDTO);

        LoginResponseDTO result = loginService.login(loginDTO);

        assertEquals("john", result.username());
        assertEquals("john@mail.com", result.email());
        assertEquals("jwt-token", result.token());
        assertEquals(roleDTO, result.role());
        assertEquals(empDTO, result.employee());
        verify(jwtGeneratorService).generateToken(user);
    }

    @Test
    void login_shouldReturnNullEmployeeWhenNotFound() throws NotFoundException {
        LoginDTO loginDTO = new LoginDTO("john", "raw-pass");
        UserModel user = buildUser(1L, "john", "john@mail.com", true);

        UserDTO.RoleInfoDTO roleDTO = new UserDTO.RoleInfoDTO(7L, "SYSTEM_ADMIN", "Administrador");
        UserDTO userDTO = new UserDTO(1L, "john", "john@mail.com", true, roleDTO, null);

        when(userService.getUserByUsername("john")).thenReturn(user);
        when(passwordEncoder.matches("raw-pass", "hashed")).thenReturn(true);
        when(jwtGeneratorService.generateToken(user)).thenReturn("jwt-token");
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userMapper.userToUserDTO(user, null)).thenReturn(userDTO);

        LoginResponseDTO result = loginService.login(loginDTO);

        assertNull(result.employee());
        assertEquals("jwt-token", result.token());
    }

    @Test
    void login_shouldThrowBadCredentialsWhenPasswordIsInvalid() throws NotFoundException {
        LoginDTO loginDTO = new LoginDTO("john", "wrong-pass");
        UserModel user = buildUser(1L, "john", "john@mail.com", true);

        when(userService.getUserByUsername("john")).thenReturn(user);
        when(passwordEncoder.matches("wrong-pass", "hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> loginService.login(loginDTO));
        verify(jwtGeneratorService, never()).generateToken(user);
        verify(employeeRepository, never()).findByUserId(1L);
    }

    @Test
    void login_shouldThrowBadCredentialsWhenUserDoesNotExist() throws NotFoundException {
        LoginDTO loginDTO = new LoginDTO("ghost", "any-pass");
        when(userService.getUserByUsername("ghost")).thenThrow(new NotFoundException("not found"));

        assertThrows(BadCredentialsException.class, () -> loginService.login(loginDTO));
        verifyNoInteractions(passwordEncoder, jwtGeneratorService, employeeRepository);
    }

    @Test
    void login_shouldThrowBadCredentialsWhenUserIsInactive() throws NotFoundException {
        LoginDTO loginDTO = new LoginDTO("john", "raw-pass");
        UserModel user = buildUser(1L, "john", "john@mail.com", false);

        when(userService.getUserByUsername("john")).thenReturn(user);
        when(passwordEncoder.matches("raw-pass", "hashed")).thenReturn(true);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> loginService.login(loginDTO));
        assertTrue(ex.getMessage().contains("desactivada"));
        verify(jwtGeneratorService, never()).generateToken(any());
    }

    private UserModel buildUser(Long id, String username, String email, boolean active) {
        RoleModel role = new RoleModel();
        role.setId(7L);
        role.setCode(RolesEnum.SYSTEM_ADMIN);
        role.setName("Administrador");

        UserModel user = new UserModel();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(active);
        user.setPassword_hash("hashed");
        user.setRole(role);
        return user;
    }

    private EmployeeModel buildEmployee(Long id, UserModel user) {
        EmployeeModel emp = new EmployeeModel();
        emp.setId(id);
        emp.setUser(user);
        emp.setFirst_name("John");
        emp.setLast_name("Doe");
        emp.setHourly_rate(50.0);
        return emp;
    }
}
