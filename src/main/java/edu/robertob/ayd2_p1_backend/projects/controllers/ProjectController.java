package edu.robertob.ayd2_p1_backend.projects.controllers;

import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.AssignProjectAdminDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.CreateProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.ProjectFilterDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.UpdateProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.response.ProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.services.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Crear proyecto",
            description = "Crea un nuevo proyecto con estado ACTIVE. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Proyecto creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ProjectDTO createProject(@RequestBody @Valid CreateProjectDTO dto) {
        return projectService.createProject(dto);
    }

    @Operation(
            summary = "Listar proyectos (paginado y filtrado)",
            description = """
                    Devuelve una página de proyectos. Solo accesible para SYSTEM_ADMIN.

                    **Filtros disponibles (query params):**
                    - `search` – búsqueda parcial en nombre del proyecto
                    - `status` – estado del proyecto: `ACTIVE` | `INACTIVE`

                    **Paginación:**
                    - `page` – número de página (inicia en 0, default: 0)
                    - `size` – tamaño de página (default: 10, máximo: 100)
                    - `sortBy` – campo de ordenamiento: `name` | `status` | `createdAt` (default: `createdAt`)
                    - `sortDir` – dirección: `asc` | `desc` (default: `desc`)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página de proyectos"),
                    @ApiResponse(responseCode = "400", description = "Estado inválido"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public PagedResponseDTO<ProjectDTO> getProjects(@ModelAttribute ProjectFilterDTO filter) {
        return projectService.getProjects(filter);
    }

    @Operation(
            summary = "Ver detalle de proyecto",
            description = "Devuelve el detalle de un proyecto por su ID. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proyecto encontrado"),
                    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ProjectDTO getProjectById(@PathVariable Long projectId) throws NotFoundException {
        return projectService.getProjectById(projectId);
    }

    @Operation(
            summary = "Editar proyecto",
            description = "Actualiza parcialmente el nombre y/o descripción de un proyecto. " +
                    "Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proyecto actualizado"),
                    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ProjectDTO updateProject(@PathVariable Long projectId,
                                    @RequestBody @Valid UpdateProjectDTO dto) throws NotFoundException {
        return projectService.updateProject(projectId, dto);
    }

    @Operation(
            summary = "Activar / desactivar proyecto",
            description = "Alterna el estado del proyecto entre ACTIVE e INACTIVE. " +
                    "Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado cambiado"),
                    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/{projectId}/toggle-status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ProjectDTO toggleStatus(@PathVariable Long projectId) throws NotFoundException {
        return projectService.toggleStatus(projectId);
    }

    @Operation(
            summary = "Asignar administrador de proyecto",
            description = "Asigna un usuario con rol PROJECT_ADMIN como administrador del proyecto. " +
                    "Desactiva la asignación previa si existe. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Administrador asignado"),
                    @ApiResponse(responseCode = "400", description = "El usuario no tiene el rol PROJECT_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Proyecto o usuario no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PostMapping("/{projectId}/admins")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ProjectDTO assignAdmin(@PathVariable Long projectId,
                                  @RequestBody @Valid AssignProjectAdminDTO dto) throws NotFoundException {
        return projectService.assignAdmin(projectId, dto);
    }
}
