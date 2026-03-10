package edu.robertob.ayd2_p1_backend.auth.login.controllers;

import edu.robertob.ayd2_p1_backend.auth.login.models.dto.request.LoginDTO;
import edu.robertob.ayd2_p1_backend.auth.login.models.dto.response.LoginResponseDTO;
import edu.robertob.ayd2_p1_backend.auth.login.services.LoginService;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    @Test
    void login_returnsLoginResponseDTO() {
        LoginDTO loginDTO = new LoginDTO("alice", "password123");
        UserDTO.RoleInfoDTO roleDTO = new UserDTO.RoleInfoDTO(1L, "DEVELOPER", "Developer");
        LoginResponseDTO expected = new LoginResponseDTO(
                "alice", "alice@mail.com", true, true, "jwt-token", roleDTO, null);
        when(loginService.login(loginDTO)).thenReturn(expected);

        LoginResponseDTO result = loginController.login(loginDTO);

        assertSame(expected, result);
        verify(loginService).login(loginDTO);
    }
}
