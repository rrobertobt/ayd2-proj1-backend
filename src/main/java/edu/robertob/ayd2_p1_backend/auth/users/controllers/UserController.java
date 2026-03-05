package edu.robertob.ayd2_p1_backend.auth.users.controllers;

import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

    /**
     * Crea un nuevo usuario del sistema. Solo accesible por usuarios con rol ADMIN.
     *
     * @param createUserDTO datos del usuario a registrar
     * @return datos básicos del usuario creado
     * @throws NotFoundException si no se encuentra ningún rol con la
     *                           etiqueta
     *                           proporcionada
     */
    @Operation(summary = "Crear nuevo usuario", description = "Crea un nuevo usuario del sistema (no participante). Solo accesible para usuarios con rol `ADMIN`.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Si el rol `Participante` intenta asiganrse,"),
            @ApiResponse(responseCode = "409", description = "Nombre de usuario ya registrado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado al recurso (requiere rol `ADMIN`), Token inválido o no proporcionado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor al procesar la solicitud")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO createNonParticipantUser(
            @RequestBody @Valid CreateUserDTO createUserDTO)
            throws NotFoundException {
        UserModel user = userService.createUser(createUserDTO);
        return userMapper.userToUserDTO(user);
    }

    

    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID único del usuario a buscar
     * @return DTO del usuario encontrado
     * @throws NotFoundException si no se encuentra un usuario con el ID dado
     */
    @Operation(summary = "Buscar usuario por ID", description = "Busca un usuario específico por su ID. Solo accesible para usuarios con rol `ADMIN`.", security = @SecurityRequirement(name = "bearerAuth"), responses = {

            @ApiResponse(responseCode = "200", description = "Usuario encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado al recurso (requiere rol `ADMIN`), Token inválido o no proporcionado"),

    })
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO findUserById(@PathVariable Long userId) throws NotFoundException {
        UserModel user = userService.getUserById(userId);
        return userMapper.userToUserDTO(user);
    }
    
    /**
     * Devuelve la información del usuario actualmente autenticado.
     *
     * @param userDetails información del usuario extraída del contexto de seguridad
     * @return DTO con información básica del usuario autenticado
     * @throws NotFoundException
     */
    @Operation(summary = "Obtener usuario autenticado", description = "Devuelve la información del usuario autenticado basado en el token JWT proporcionado.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Usuario autenticado encontrado"),
            @ApiResponse(responseCode = "403", description = "Token inválido o no proporcionado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/me")
    public UserDTO getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails)
            throws NotFoundException {
        UserModel user = userService.getUserByUsername(userDetails.getUsername());
        return userMapper.userToUserDTO(user);
    }
}