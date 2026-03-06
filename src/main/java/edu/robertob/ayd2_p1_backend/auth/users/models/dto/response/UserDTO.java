package edu.robertob.ayd2_p1_backend.auth.users.models.dto.response;

public record UserDTO(
        Long id,
        String username,
        String email,
        boolean active,
        RoleInfoDTO role,
        EmployeeDTO employee
) {
    public record RoleInfoDTO(Long id, String code, String name) {}

    public record EmployeeDTO(
            Long id,
            String firstName,
            String lastName,
            Double hourlyRate
    ) {}
}
