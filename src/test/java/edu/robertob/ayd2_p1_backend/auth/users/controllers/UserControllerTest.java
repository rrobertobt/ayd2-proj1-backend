package edu.robertob.ayd2_p1_backend.auth.users.controllers;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.SetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UpdateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UserFilterDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserMeDTO;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserManagementService;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private UserManagementService userManagementService;

    @InjectMocks
    private UserController userController;

    @Test
    void getAuthenticatedUser_returnsUserMeDTO() throws NotFoundException {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("alice");
        UserMeDTO expected = new UserMeDTO(1L, "alice", "alice@mail.com", true,
                new UserDTO.RoleInfoDTO(1L, "DEVELOPER", "Developer"), null);
        when(userService.getMeByUsername("alice")).thenReturn(expected);

        UserMeDTO result = userController.getAuthenticatedUser(userDetails);

        assertSame(expected, result);
        verify(userService).getMeByUsername("alice");
    }

    @Test
    void setPasswordFromOnboarding_callsManagementService() throws InvalidTokenException {
        SetPasswordDTO dto = mock(SetPasswordDTO.class);
        doNothing().when(userManagementService).setPasswordFromOnboarding(dto);

        userController.setPasswordFromOnboarding(dto);

        verify(userManagementService).setPasswordFromOnboarding(dto);
    }

    @Test
    void getAllUsers_returnsPagedResponse() {
        UserFilterDTO filter = new UserFilterDTO();
        PagedResponseDTO<UserDTO> expected =
                new PagedResponseDTO<>(List.of(), 0, 10, 0L, 0, true);
        when(userManagementService.getUsers(filter)).thenReturn(expected);

        PagedResponseDTO<UserDTO> result = userController.getAllUsers(filter);

        assertSame(expected, result);
        verify(userManagementService).getUsers(filter);
    }

    @Test
    void createUser_returnsCreatedUserDTO() throws NotFoundException {
        CreateUserDTO dto = mock(CreateUserDTO.class);
        UserDTO expected = new UserDTO(2L, "bob", "bob@mail.com", true,
                new UserDTO.RoleInfoDTO(1L, "DEVELOPER", "Developer"), null);
        when(userManagementService.createUser(dto)).thenReturn(expected);

        UserDTO result = userController.createUser(dto);

        assertSame(expected, result);
    }

    @Test
    void getUserById_returnsUserDTO() throws NotFoundException {
        UserDTO expected = new UserDTO(3L, "carol", "carol@mail.com", true,
                new UserDTO.RoleInfoDTO(1L, "DEVELOPER", "Developer"), null);
        when(userManagementService.getUserById(3L)).thenReturn(expected);

        UserDTO result = userController.getUserById(3L);

        assertSame(expected, result);
    }

    @Test
    void updateUser_returnsUpdatedUserDTO() throws NotFoundException {
        UpdateUserDTO dto = mock(UpdateUserDTO.class);
        UserDTO expected = new UserDTO(4L, "dave", "dave@mail.com", true,
                new UserDTO.RoleInfoDTO(1L, "DEVELOPER", "Developer"), null);
        when(userManagementService.updateUser(4L, dto)).thenReturn(expected);

        UserDTO result = userController.updateUser(4L, dto);

        assertSame(expected, result);
    }

    @Test
    void toggleUserStatus_returnsToggledUserDTO() throws NotFoundException {
        UserDTO expected = new UserDTO(5L, "eve", "eve@mail.com", false,
                new UserDTO.RoleInfoDTO(1L, "DEVELOPER", "Developer"), null);
        when(userManagementService.toggleUserStatus(5L)).thenReturn(expected);

        UserDTO result = userController.toggleUserStatus(5L);

        assertSame(expected, result);
    }
}
