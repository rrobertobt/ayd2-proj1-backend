package edu.robertob.ayd2_p1_backend.auth.login.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class LoginDTO {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password;
}
