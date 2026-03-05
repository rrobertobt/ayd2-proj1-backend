package edu.robertob.ayd2_p1_backend.auth.login.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.robertob.ayd2_p1_backend.auth.login.models.dto.request.LoginDTO;
import edu.robertob.ayd2_p1_backend.auth.login.models.dto.response.LoginResponseDTO;
import edu.robertob.ayd2_p1_backend.auth.login.services.LoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * Endpoint para autenticar a un usuario.
     * Recibe credenciales y retorna un token JWT si son válidas.
     *
     * @param loginDTO objeto con el nombre de usuario y la contraseña
     * @return LoginResponseDTO con el token, nombre de usuario y rol
     */
    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario con su nombre de usuario y contraseña. Retorna un token JWT si las credenciales son válidas.", responses = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO login(@RequestBody @Valid LoginDTO loginDTO) {
        System.out.println("Recibida solicitud de login para el usuario: " + loginDTO.getUsername());
        return loginService.login(loginDTO);
    }

}
