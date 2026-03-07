package edu.robertob.ayd2_p1_backend.core.config;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.services.RoleService;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.services.EmployeeService;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeedersConfigTest {

    @Mock private RoleService roleService;
    @Mock private UserService userService;
    @Mock private EmployeeService employeeService;

    @InjectMocks
    private SeedersConfig seedersConfig;

    @Test
    void run_successfulSeed_callsAllServices() throws Exception {
        RoleModel systemAdminRole = new RoleModel(1L, RolesEnum.SYSTEM_ADMIN, "System Administrator", "Full access");
        UserModel superAdmin = mock(UserModel.class);

        when(roleService.findRoleByCodeOrNull(RolesEnum.SYSTEM_ADMIN)).thenReturn(systemAdminRole);
        when(userService.createUserIfNotExists(anyString(), anyString(), anyString(), anyBoolean(), any(RoleModel.class)))
                .thenReturn(superAdmin);

        assertDoesNotThrow(() -> seedersConfig.run());

        verify(roleService, times(3)).createRoleIfNotExists(any(RolesEnum.class), anyString(), anyString());
        verify(roleService).findRoleByCodeOrNull(RolesEnum.SYSTEM_ADMIN);
        verify(userService).createUserIfNotExists(eq("superadmin"), eq("superadmin@tracker.com"), anyString(), eq(true), eq(systemAdminRole));
        verify(employeeService).createEmployeeIfNotExists(eq(superAdmin), eq("Super"), eq("Admin"), eq(0.00d));
    }

    @Test
    void run_systemAdminRoleNotFound_throwsIllegalStateException() {
        when(roleService.findRoleByCodeOrNull(RolesEnum.SYSTEM_ADMIN)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> seedersConfig.run());
    }
}
