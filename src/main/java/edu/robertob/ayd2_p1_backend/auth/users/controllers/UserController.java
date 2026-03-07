package edu.robertob.ayd2_p1_backend.auth.users.controllers;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.SetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UpdateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UserFilterDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserMeDTO;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserManagementService;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserManagementService userManagementService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /me  – authenticated user info (no role restriction)
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Obtener usuario autenticado",
            description = "Devuelve la información del usuario autenticado basado en el token JWT.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Información del usuario autenticado"),
                    @ApiResponse(responseCode = "401", description = "Token inválido o no proporcionado")
            })
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserMeDTO getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails)
            throws NotFoundException {
        return userService.getMeByUsername(userDetails.getUsername());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC – onboarding set-password (no auth required)
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Establecer contraseña mediante token de onboarding",
            description = "Endpoint público. Valida el token enviado por correo y establece la contraseña del usuario.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña establecida exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Token inválido, expirado, o contraseñas no coinciden")
            })
    @PostMapping("/onboarding/set-password")
    @ResponseStatus(HttpStatus.OK)
    public void setPasswordFromOnboarding(@RequestBody @Valid SetPasswordDTO dto)
            throws InvalidTokenException {
        userManagementService.setPasswordFromOnboarding(dto);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN – user management (SYSTEM_ADMIN only)
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Listar usuarios (paginado y filtrado)",
            description = """
                    Devuelve una página de usuarios del sistema. Solo accesible para SYSTEM_ADMIN.

                    **Filtros disponibles (query params):**
                    - `search` – búsqueda parcial en username y email
                    - `firstName` – búsqueda parcial en nombre del empleado
                    - `lastName` – búsqueda parcial en apellido del empleado
                    - `email` – email exacto
                    - `roleId` – ID del rol
                    - `roleCode` – código del rol: `SYSTEM_ADMIN` | `PROJECT_ADMIN` | `DEVELOPER`
                    - `active` – true / false

                    **Paginación:**
                    - `page` – número de página (inicia en 0, default: 0)
                    - `size` – tamaño de página (default: 10, máximo: 100)
                    - `sortBy` – campo de ordenamiento: `username` | `email` | `active` | `createdAt` | `firstName` | `lastName` | `hourlyRate` (default: `createdAt`)
                    - `sortDir` – dirección: `asc` | `desc` (default: `desc`)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página de usuarios"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public PagedResponseDTO<UserDTO> getAllUsers(@ModelAttribute UserFilterDTO filter) {
        return userManagementService.getUsers(filter);
    }

    @Operation(
            summary = "Crear nuevo usuario",
            description = "Crea un nuevo usuario junto con su perfil de empleado. " +
                    "Si no se proporciona contraseña se envía un correo de onboarding. " +
                    "Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Username o email ya registrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public UserDTO createUser(@RequestBody @Valid CreateUserDTO dto) throws NotFoundException {
        return userManagementService.createUser(dto);
    }

    @Operation(
            summary = "Buscar usuario por ID",
            description = "Obtiene un usuario específico por su ID. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public UserDTO getUserById(@PathVariable Long userId) throws NotFoundException {
        return userManagementService.getUserById(userId);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza parcialmente los datos de un usuario y su perfil de empleado. " +
                    "Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Username o email ya en uso"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public UserDTO updateUser(@PathVariable Long userId,
                              @RequestBody @Valid UpdateUserDTO dto) throws NotFoundException {
        return userManagementService.updateUser(userId, dto);
    }

    @Operation(
            summary = "Activar / desactivar usuario",
            description = "Cambia el estado activo/inactivo de un usuario. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado cambiado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/{userId}/toggle-status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public UserDTO toggleUserStatus(@PathVariable Long userId,
                                    @AuthenticationPrincipal UserDetails userDetails) throws NotFoundException {
        return userManagementService.toggleUserStatus(userId, userDetails.getUsername());
    }

    @Operation(
            summary = "Reenviar correo de onboarding",
            description = "Reenvía el correo de onboarding a un usuario inactivo que no ha completado el proceso. " +
                    "Invalida cualquier token anterior y genera uno nuevo. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Correo reenviado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "El usuario ya completó el onboarding y está activo"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PostMapping("/{userId}/onboarding/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public void resendOnboardingEmail(@PathVariable Long userId) throws NotFoundException {
        userManagementService.resendOnboardingEmail(userId);
    }
}