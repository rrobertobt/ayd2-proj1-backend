package edu.robertob.ayd2_p1_backend.auth.users.models.entities;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserEntitiesTest {

    // ── UserModel ─────────────────────────────────────────────────────────────

    @Test
    void userModel_gettersAndSetters() {
        RoleModel role = new RoleModel();
        role.setId(1L);
        role.setCode(RolesEnum.DEVELOPER);
        role.setName("Developer");

        UserModel user = new UserModel();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@mail.com");
        user.setPassword_hash("hashed");
        user.setActive(true);
        user.setOnboardingCompleted(true);
        user.setRole(role);

        assertEquals(1L, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@mail.com", user.getEmail());
        assertEquals("hashed", user.getPassword_hash());
        assertTrue(user.isActive());
        assertTrue(user.isOnboardingCompleted());
        assertSame(role, user.getRole());
        assertNotNull(user.toString());
    }

    @Test
    void userModel_constructorWithArgs() {
        RoleModel role = new RoleModel();
        UserModel user = new UserModel(5L, "bob", "bob@mail.com", "pass", role);

        assertEquals(5L, user.getId());
        assertEquals("bob", user.getUsername());
    }

    @Test
    void userModel_equalsAndHashCode() {
        UserModel u1 = new UserModel();
        u1.setId(1L);
        UserModel u2 = new UserModel();
        u2.setId(1L);

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    // ── EmployeeModel ─────────────────────────────────────────────────────────

    @Test
    void employeeModel_gettersAndSetters() {
        UserModel user = new UserModel();
        user.setId(2L);

        EmployeeModel emp = new EmployeeModel();
        emp.setId(10L);
        emp.setFirst_name("John");
        emp.setLast_name("Doe");
        emp.setHourly_rate(25.0);
        emp.setUser(user);

        assertEquals(10L, emp.getId());
        assertEquals("John", emp.getFirst_name());
        assertEquals("Doe", emp.getLast_name());
        assertEquals(25.0, emp.getHourly_rate());
        assertSame(user, emp.getUser());
        assertNotNull(emp.toString());
    }

    @Test
    void employeeModel_equalsAndHashCode() {
        EmployeeModel e1 = new EmployeeModel();
        e1.setId(1L);
        EmployeeModel e2 = new EmployeeModel();
        e2.setId(1L);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    // ── OnboardingTokenModel ──────────────────────────────────────────────────

    @Test
    void onboardingTokenModel_gettersAndSetters() {
        UserModel user = new UserModel();
        user.setId(3L);
        Instant expires = Instant.now().plusSeconds(3600);

        OnboardingTokenModel token = new OnboardingTokenModel();
        token.setId(1L);
        token.setUser(user);
        token.setToken("abc123");
        token.setUsed(false);
        token.setExpiresAt(expires);

        assertEquals(1L, token.getId());
        assertSame(user, token.getUser());
        assertEquals("abc123", token.getToken());
        assertFalse(token.isUsed());
        assertEquals(expires, token.getExpiresAt());
        assertNotNull(token.toString());
    }

    // ── PasswordResetTokenModel ───────────────────────────────────────────────

    @Test
    void passwordResetTokenModel_gettersAndSetters() {
        UserModel user = new UserModel();
        user.setId(4L);
        Instant expires = Instant.now().plusSeconds(1800);

        PasswordResetTokenModel token = new PasswordResetTokenModel();
        token.setId(2L);
        token.setUser(user);
        token.setToken("reset-xyz");
        token.setUsed(true);
        token.setExpiresAt(expires);

        assertEquals(2L, token.getId());
        assertSame(user, token.getUser());
        assertEquals("reset-xyz", token.getToken());
        assertTrue(token.isUsed());
        assertEquals(expires, token.getExpiresAt());
        assertNotNull(token.toString());
    }
}
