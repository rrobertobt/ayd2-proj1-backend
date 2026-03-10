package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserMeDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_shouldReturnUserWhenExists() throws NotFoundException {
        UserModel user = buildUser(1L, "alice", "alice@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserModel result = userService.getUserById(1L);

        assertSame(user, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldThrowWhenMissing() {
        when(userRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(55L));
        verify(userRepository).findById(55L);
    }

    // ── getUserByUsername ─────────────────────────────────────────────────────

    @Test
    void getUserByUsername_shouldReturnUserWhenExists() throws NotFoundException {
        UserModel user = buildUser(2L, "carlos", "carlos@mail.com");
        when(userRepository.findUserByUsername("carlos")).thenReturn(Optional.of(user));

        UserModel result = userService.getUserByUsername("carlos");

        assertSame(user, result);
        verify(userRepository).findUserByUsername("carlos");
    }

    @Test
    void getUserByUsername_shouldThrowWhenMissing() {
        when(userRepository.findUserByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserByUsername("ghost"));
        verify(userRepository).findUserByUsername("ghost");
    }

    // ── getAuthenticatedUserByUsername ────────────────────────────────────────

    @Test
    void getAuthenticatedUserByUsername_shouldMapUserWithEmployee() throws NotFoundException {
        UserModel user = buildUser(9L, "sara", "sara@mail.com");
        EmployeeModel employee = buildEmployee(4L, user);
        UserDTO.RoleInfoDTO roleDTO = new UserDTO.RoleInfoDTO(3L, "PROJECT_ADMIN", "Project Admin");
        UserDTO.EmployeeDTO empDTO = new UserDTO.EmployeeDTO(4L, "Sara", "Lopez", 25.0);
        UserDTO dto = new UserDTO(9L, "sara", "sara@mail.com", true, true, roleDTO, empDTO);

        when(userRepository.findUserByUsername("sara")).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(9L)).thenReturn(Optional.of(employee));
        when(userMapper.userToUserDTO(user, employee)).thenReturn(dto);

        UserDTO result = userService.getAuthenticatedUserByUsername("sara");

        assertSame(dto, result);
        verify(userRepository).findUserByUsername("sara");
        verify(employeeRepository).findByUserId(9L);
        verify(userMapper).userToUserDTO(user, employee);
    }

    @Test
    void getAuthenticatedUserByUsername_shouldThrowWhenUserMissing() {
        when(userRepository.findUserByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getAuthenticatedUserByUsername("missing"));
        verify(userRepository).findUserByUsername("missing");
    }

    // ── getMeByUsername ───────────────────────────────────────────────────────

    @Test
    void getMeByUsername_shouldReturnUserMeDTO() throws NotFoundException {
        UserModel user = buildUser(10L, "bob", "bob@mail.com");
        EmployeeModel employee = buildEmployee(5L, user);
        UserDTO.RoleInfoDTO roleDTO = new UserDTO.RoleInfoDTO(3L, "DEVELOPER", "Developer");
        UserDTO.EmployeeDTO empDTO = new UserDTO.EmployeeDTO(5L, "Bob", "Smith", 30.0);
        UserMeDTO meDTO = new UserMeDTO(10L, "bob", "bob@mail.com", true, true, roleDTO, empDTO);

        when(userRepository.findUserByUsername("bob")).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(employee));
        when(userMapper.userToUserMeDTO(user, employee)).thenReturn(meDTO);

        UserMeDTO result = userService.getMeByUsername("bob");

        assertSame(meDTO, result);
        verify(userMapper).userToUserMeDTO(user, employee);
    }

    // ── count ─────────────────────────────────────────────────────────────────

    @Test
    void count_shouldReturnRepositoryCount() {
        when(userRepository.count()).thenReturn(13L);

        Long result = userService.count();

        assertEquals(13L, result);
        verify(userRepository).count();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UserModel buildUser(Long id, String username, String email) {
        RoleModel role = buildRole(3L, RolesEnum.PROJECT_ADMIN, "Project Admin");
        UserModel user = new UserModel();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(true);
        user.setRole(role);
        user.setPassword_hash("secret");
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
}
