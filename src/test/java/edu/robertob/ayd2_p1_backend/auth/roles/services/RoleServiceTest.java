package edu.robertob.ayd2_p1_backend.auth.roles.services;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.repositories.RoleRepository;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void findAllRoles_shouldReturnAllRoles() {
        RoleModel admin = buildRole(1L, RolesEnum.SYSTEM_ADMIN, "Administrador");
        RoleModel dev = buildRole(2L, RolesEnum.DEVELOPER, "Developer");
        when(roleRepository.findAll()).thenReturn(List.of(admin, dev));

        List<RoleModel> result = roleService.findAllRoles();

        assertEquals(2, result.size());
        assertEquals(RolesEnum.SYSTEM_ADMIN, result.get(0).getCode());
        assertEquals(RolesEnum.DEVELOPER, result.get(1).getCode());
        verify(roleRepository).findAll();
    }

    @Test
    void findRoleByName_shouldReturnRoleWhenExists() throws NotFoundException {
        RoleModel role = buildRole(1L, RolesEnum.PROJECT_ADMIN, "Project Admin");
        when(roleRepository.findByName("Project Admin")).thenReturn(Optional.of(role));

        RoleModel result = roleService.findRoleByName("Project Admin");

        assertNotNull(result);
        assertEquals(RolesEnum.PROJECT_ADMIN, result.getCode());
        verify(roleRepository).findByName("Project Admin");
    }

    @Test
    void findRoleByName_shouldThrowWhenRoleDoesNotExist() {
        when(roleRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.findRoleByName("Missing"));
        verify(roleRepository).findByName("Missing");
    }

    @Test
    void findRoleByCode_shouldReturnRoleWhenExists() throws NotFoundException {
        RoleModel role = buildRole(3L, RolesEnum.DEVELOPER, "Developer");
        when(roleRepository.findByCode(RolesEnum.DEVELOPER)).thenReturn(Optional.of(role));

        RoleModel result = roleService.findRoleByCode(RolesEnum.DEVELOPER);

        assertEquals("Developer", result.getName());
        verify(roleRepository).findByCode(RolesEnum.DEVELOPER);
    }

    @Test
    void findRoleByCode_shouldThrowWhenRoleDoesNotExist() {
        when(roleRepository.findByCode(RolesEnum.DEVELOPER)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.findRoleByCode(RolesEnum.DEVELOPER));
        verify(roleRepository).findByCode(RolesEnum.DEVELOPER);
    }

    @Test
    void findRoleById_shouldReturnRoleWhenExists() throws NotFoundException {
        RoleModel role = buildRole(9L, RolesEnum.SYSTEM_ADMIN, "Administrador");
        when(roleRepository.findById(9L)).thenReturn(Optional.of(role));

        RoleModel result = roleService.findRoleById(9L);

        assertEquals(RolesEnum.SYSTEM_ADMIN, result.getCode());
        verify(roleRepository).findById(9L);
    }

    @Test
    void findRoleById_shouldThrowWhenRoleDoesNotExist() {
        when(roleRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.findRoleById(404L));
        verify(roleRepository).findById(404L);
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
