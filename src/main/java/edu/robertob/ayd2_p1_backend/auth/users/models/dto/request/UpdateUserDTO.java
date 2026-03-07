package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class UpdateUserDTO {

    @Size(max = 100, message = "El nombre de usuario debe tener como máximo 100 caracteres.")
    private String username;

    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 200, message = "El correo electrónico debe tener como máximo 200 caracteres.")
    private String email;

    private Long roleId;

    @Valid
    private EmployeeUpdateDataDTO employee;

    @Getter
    public static class EmployeeUpdateDataDTO {

        @Size(max = 120, message = "El nombre debe tener como máximo 120 caracteres.")
        private String firstName;

        @Size(max = 120, message = "El apellido debe tener como máximo 120 caracteres.")
        private String lastName;

        @DecimalMin(value = "0.0", message = "La tarifa por hora no puede ser negativa.")
        private Double hourlyRate;
    }
}

