package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateUserDTO {

    @NotBlank(message = "Por favor, ingresa un nombre de usuario.")
    @Size(max = 50, message = "El nombre de usuario debe tener como máximo 50 caracteres.")
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    String password;

    @NotBlank(message = "El rol no puede estar vacío")
    private Long roleId;

    public CreateUserDTO(
            @NotBlank(message = "Por favor, ingresa un nombre de usuario.") @Size(max = 50, message = "El nombre de usuario debe tener como máximo 50 caracteres.") String username,
            @NotBlank(message = "Por favor, ingresa una contraseña.") @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres.") String password,
            @NotBlank(message = "El rol no puede estar vacío") Long roleId
    ) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
    }

}
