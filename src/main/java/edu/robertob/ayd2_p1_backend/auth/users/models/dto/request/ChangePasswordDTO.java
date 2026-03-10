package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordDTO {

    @NotBlank(message = "La contraseña actual no puede estar vacía.")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña no puede estar vacía.")
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres.")
    private String newPassword;

    @NotBlank(message = "La confirmación de contraseña no puede estar vacía.")
    private String confirmNewPassword;
}
