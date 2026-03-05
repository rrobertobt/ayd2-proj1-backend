package edu.robertob.ayd2_p1_backend.auth.login.models.dto.response;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;

public record LoginResponseDTO(String username, String email, boolean active, String token, RoleModel role, EmployeeModel employee) {}
