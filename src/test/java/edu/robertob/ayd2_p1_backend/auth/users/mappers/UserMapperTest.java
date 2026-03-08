package edu.robertob.ayd2_p1_backend.auth.users.mappers;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserMeDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void userToUserDTO_withEmployeeAndRole_mapsAllFields() {
        UserModel user = buildUser(1L, "alice", "alice@mail.com", true);
        EmployeeModel emp = buildEmployee(2L, user);

        UserDTO result = userMapper.userToUserDTO(user, emp);

        assertEquals(1L, result.id());
        assertEquals("alice", result.username());
        assertEquals("alice@mail.com", result.email());
        assertTrue(result.active());
        assertFalse(result.onboardingCompleted());

        assertNotNull(result.role());
        assertEquals(10L, result.role().id());
        assertEquals("DEVELOPER", result.role().code());
        assertEquals("Developer", result.role().name());

        assertNotNull(result.employee());
        assertEquals(2L, result.employee().id());
        assertEquals("First", result.employee().firstName());
        assertEquals("Last", result.employee().lastName());
        assertEquals(25.0, result.employee().hourlyRate());
    }

    @Test
    void userToUserDTO_withNullEmployee_employeeDTOIsNull() {
        UserModel user = buildUser(3L, "bob", "bob@mail.com", true);

        UserDTO result = userMapper.userToUserDTO(user, null);

        assertNotNull(result);
        assertNull(result.employee());
        assertNotNull(result.role());
    }

    @Test
    void userToUserDTO_withNullRole_roleDTOIsNull() {
        UserModel user = buildUser(4L, "carol", "carol@mail.com", true);
        user.setRole(null);

        UserDTO result = userMapper.userToUserDTO(user, null);

        assertNotNull(result);
        assertNull(result.role());
    }

    @Test
    void userToUserDTO_singleParam_delegatesToTwoParam() {
        UserModel user = buildUser(5L, "dave", "dave@mail.com", false);

        UserDTO result = userMapper.userToUserDTO(user);

        assertEquals(5L, result.id());
        assertNull(result.employee());
    }

    @Test
    void userToUserMeDTO_returnsMeDTOWithSameFields() {
        UserModel user = buildUser(6L, "eve", "eve@mail.com", true);
        EmployeeModel emp = buildEmployee(3L, user);

        UserMeDTO result = userMapper.userToUserMeDTO(user, emp);

        assertEquals(6L, result.id());
        assertEquals("eve", result.username());
        assertEquals("eve@mail.com", result.email());
        assertTrue(result.active());
        assertNotNull(result.role());
        assertNotNull(result.employee());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UserModel buildUser(Long id, String username, String email, boolean active) {
        RoleModel role = new RoleModel();
        role.setId(10L);
        role.setCode(RolesEnum.DEVELOPER);
        role.setName("Developer");
        role.setDescription("Developer role");

        UserModel user = new UserModel();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(active);
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
}
