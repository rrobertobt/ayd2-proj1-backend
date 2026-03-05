package edu.robertob.ayd2_p1_backend.auth.users.models.dto.response;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;

public record UserMeDTO (
        String username,
        String email,
        boolean active,
        RoleModel role,
        EmployeeModel employee
) {
}
