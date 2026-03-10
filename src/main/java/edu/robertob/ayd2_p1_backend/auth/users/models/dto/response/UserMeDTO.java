package edu.robertob.ayd2_p1_backend.auth.users.models.dto.response;

public record UserMeDTO(
        Long id,
        String username,
        String email,
        boolean active,
        boolean onboardingCompleted,
        UserDTO.RoleInfoDTO role,
        UserDTO.EmployeeDTO employee
) {}
