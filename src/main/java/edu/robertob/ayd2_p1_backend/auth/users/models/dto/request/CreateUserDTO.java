package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class CreateUserDTO {

    @NotBlank(message = "Por favor, ingresa un nombre de usuario.")
    @Size(max = 100, message = "El nombre de usuario debe tener como máximo 100 caracteres.")
    private String username;

    @NotBlank(message = "El correo electrónico no puede estar vacío.")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 200, message = "El correo electrónico debe tener como máximo 200 caracteres.")
    private String email;

    /**
     * Contraseña inicial opcional.
     * Si se proporciona, se establece directamente.
     * Si es null o vacía, se enviará un código de onboarding al correo del usuario.
     */
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres.")
    private String password;

    @NotNull(message = "El ID del rol no puede estar vacío.")
    private Long roleId;

    @Valid
    @NotNull(message = "Los datos del empleado son requeridos.")
    private EmployeeDataDTO employee;

    @Getter
    public static class EmployeeDataDTO {

        @NotBlank(message = "El nombre del empleado no puede estar vacío.")
        @Size(max = 120, message = "El nombre debe tener como máximo 120 caracteres.")
        private String firstName;

        @NotBlank(message = "El apellido del empleado no puede estar vacío.")
        @Size(max = 120, message = "El apellido debe tener como máximo 120 caracteres.")
        private String lastName;

        @NotNull(message = "La tarifa por hora no puede ser nula.")
        @DecimalMin(value = "0.0", message = "La tarifa por hora no puede ser negativa.")
        private Double hourlyRate;
    }
}
