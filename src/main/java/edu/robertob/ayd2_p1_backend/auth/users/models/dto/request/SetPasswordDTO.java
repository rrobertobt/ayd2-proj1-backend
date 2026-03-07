package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SetPasswordDTO {

    @NotBlank(message = "El token de onboarding no puede estar vacío.")
    private String token;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres.")
    private String password;

    @NotBlank(message = "La confirmación de contraseña no puede estar vacía.")
    private String confirmPassword;
}

