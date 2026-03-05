package edu.robertob.ayd2_p1_backend.auth.login.models.dto.response;

import lombok.Value;

@Value
public class LoginResponseDTO {

    String userName;
    String role;
    String token;
}
