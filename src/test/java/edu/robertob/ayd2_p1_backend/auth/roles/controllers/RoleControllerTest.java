package edu.robertob.ayd2_p1_backend.auth.roles.controllers;

import edu.robertob.ayd2_p1_backend.auth.roles.mappers.RoleMapper;
import edu.robertob.ayd2_p1_backend.auth.roles.models.dto.response.RoleDTO;
import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.services.RoleService;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock private RoleService roleService;
    @Mock private RoleMapper roleMapper;

    @InjectMocks
    private RoleController roleController;

    @Test
    void getAllRoles_returnsRoleDTOList() {
        RoleModel admin = buildRole(1L, RolesEnum.SYSTEM_ADMIN, "Administrador");
        RoleModel dev = buildRole(2L, RolesEnum.DEVELOPER, "Developer");
        List<RoleModel> roles = List.of(admin, dev);

        RoleDTO adminDTO = new RoleDTO(1L, "SYSTEM_ADMIN", "Administrador");
        RoleDTO devDTO = new RoleDTO(2L, "DEVELOPER", "Developer");
        List<RoleDTO> expected = List.of(adminDTO, devDTO);

        when(roleService.findAllRoles()).thenReturn(roles);
        when(roleMapper.rolesToRoleDTOList(roles)).thenReturn(expected);

        List<RoleDTO> result = roleController.getAllRoles();

        assertEquals(2, result.size());
        assertSame(expected, result);
        verify(roleService).findAllRoles();
        verify(roleMapper).rolesToRoleDTOList(roles);
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
