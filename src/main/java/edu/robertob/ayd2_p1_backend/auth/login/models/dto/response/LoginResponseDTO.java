package edu.robertob.ayd2_p1_backend.auth.login.models.dto.response;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;

public record LoginResponseDTO(
        String username,
        String email,
        boolean active,
        String token,
        UserDTO.RoleInfoDTO role,
        UserDTO.EmployeeDTO employee
) {}
