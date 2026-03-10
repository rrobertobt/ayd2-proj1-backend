package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lightweight coverage tests for @Getter-only request DTOs.
 * Uses reflection to set private fields since no setters exist.
 */
class UserRequestDTOsTest {

    private static void set(Object obj, String field, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private static void setSuper(Object obj, Class<?> clazz, String field, Object value) throws Exception {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, value);
    }

    // ── CreateUserDTO ─────────────────────────────────────────────────────────

    @Test
    void createUserDTO_gettersReturnSetValues() throws Exception {
        CreateUserDTO dto = new CreateUserDTO();
        set(dto, "username", "alice");
        set(dto, "email", "alice@mail.com");
        set(dto, "password", "secret123");
        set(dto, "roleId", 1L);

        assertEquals("alice", dto.getUsername());
        assertEquals("alice@mail.com", dto.getEmail());
        assertEquals("secret123", dto.getPassword());
        assertEquals(1L, dto.getRoleId());
        assertNull(dto.getEmployee());
    }

    @Test
    void createUserDTO_employeeDataDTO_gettersReturnSetValues() throws Exception {
        CreateUserDTO.EmployeeDataDTO emp = new CreateUserDTO.EmployeeDataDTO();
        setSuper(emp, CreateUserDTO.EmployeeDataDTO.class, "firstName", "John");
        setSuper(emp, CreateUserDTO.EmployeeDataDTO.class, "lastName", "Doe");
        setSuper(emp, CreateUserDTO.EmployeeDataDTO.class, "hourlyRate", 25.0);

        assertEquals("John", emp.getFirstName());
        assertEquals("Doe", emp.getLastName());
        assertEquals(25.0, emp.getHourlyRate());
    }

    // ── UpdateUserDTO ─────────────────────────────────────────────────────────

    @Test
    void updateUserDTO_gettersReturnSetValues() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO();
        set(dto, "username", "bob");
        set(dto, "email", "bob@mail.com");
        set(dto, "roleId", 2L);

        assertEquals("bob", dto.getUsername());
        assertEquals("bob@mail.com", dto.getEmail());
        assertEquals(2L, dto.getRoleId());
        assertNull(dto.getEmployee());
    }

    @Test
    void updateUserDTO_employeeUpdateDataDTO_gettersReturnSetValues() throws Exception {
        UpdateUserDTO.EmployeeUpdateDataDTO emp = new UpdateUserDTO.EmployeeUpdateDataDTO();
        setSuper(emp, UpdateUserDTO.EmployeeUpdateDataDTO.class, "firstName", "Jane");
        setSuper(emp, UpdateUserDTO.EmployeeUpdateDataDTO.class, "lastName", "Smith");
        setSuper(emp, UpdateUserDTO.EmployeeUpdateDataDTO.class, "hourlyRate", 30.0);

        assertEquals("Jane", emp.getFirstName());
        assertEquals("Smith", emp.getLastName());
        assertEquals(30.0, emp.getHourlyRate());
    }

    // ── ChangePasswordDTO ─────────────────────────────────────────────────────

    @Test
    void changePasswordDTO_gettersReturnSetValues() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        set(dto, "currentPassword", "oldPass");
        set(dto, "newPassword", "newPass123");
        set(dto, "confirmNewPassword", "newPass123");

        assertEquals("oldPass", dto.getCurrentPassword());
        assertEquals("newPass123", dto.getNewPassword());
        assertEquals("newPass123", dto.getConfirmNewPassword());
    }

    // ── ResetPasswordDTO ──────────────────────────────────────────────────────

    @Test
    void resetPasswordDTO_gettersReturnSetValues() throws Exception {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        set(dto, "token", "reset-token");
        set(dto, "password", "newPass123");
        set(dto, "confirmPassword", "newPass123");

        assertEquals("reset-token", dto.getToken());
        assertEquals("newPass123", dto.getPassword());
        assertEquals("newPass123", dto.getConfirmPassword());
    }

    // ── ForgotPasswordDTO ─────────────────────────────────────────────────────

    @Test
    void forgotPasswordDTO_gettersReturnSetValues() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        set(dto, "email", "user@mail.com");

        assertEquals("user@mail.com", dto.getEmail());
    }

    // ── SetPasswordDTO ────────────────────────────────────────────────────────

    @Test
    void setPasswordDTO_gettersReturnSetValues() throws Exception {
        SetPasswordDTO dto = new SetPasswordDTO();
        set(dto, "token", "onboard-token");
        set(dto, "password", "myPass123");
        set(dto, "confirmPassword", "myPass123");

        assertEquals("onboard-token", dto.getToken());
        assertEquals("myPass123", dto.getPassword());
        assertEquals("myPass123", dto.getConfirmPassword());
    }
}
