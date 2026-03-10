package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ForgotPasswordDTO {

    @NotBlank
    @Email
    @Size(max = 200)
    private String email;
}
