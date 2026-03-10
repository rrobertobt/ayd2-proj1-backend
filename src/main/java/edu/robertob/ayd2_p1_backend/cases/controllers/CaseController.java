package edu.robertob.ayd2_p1_backend.cases.controllers;

import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CancelCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CaseFilterDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.UpdateDueDateDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseSummaryDTO;
import edu.robertob.ayd2_p1_backend.cases.services.CaseService;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @Operation(
            summary = "Crear caso",
            description = "Crea un nuevo caso y genera automáticamente los pasos a partir de las etapas del tipo de caso. " +
                    "Solo accesible para PROJECT_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Caso creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de caso sin etapas"),
                    @ApiResponse(responseCode = "404", description = "Proyecto o tipo de caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PostMapping("/api/v1/cases")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    public CaseDTO createCase(@RequestBody @Valid CreateCaseDTO dto) throws NotFoundException {
        return caseService.createCase(dto);
    }

    @Operation(
            summary = "Listar casos (paginado y filtrado)",
            description = """
                    Devuelve una página de casos. Accesible para PROJECT_ADMIN y SYSTEM_ADMIN.

                    **Filtros disponibles (query params):**
                    - `projectId` – filtrar por proyecto
                    - `caseTypeId` – filtrar por tipo de caso
                    - `status` – estado del caso: `OPEN` | `IN_PROGRESS` | `COMPLETED` | `CANCELED`

                    **Paginación:**
                    - `page` – número de página (inicia en 0, default: 0)
                    - `size` – tamaño de página (default: 10, máximo: 100)
                    - `sortBy` – campo de ordenamiento: `title` | `status` | `dueDate` | `createdAt` (default: `createdAt`)
                    - `sortDir` – dirección: `asc` | `desc` (default: `desc`)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página de casos"),
                    @ApiResponse(responseCode = "400", description = "Estado inválido"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/api/v1/cases")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SYSTEM_ADMIN')")
    public PagedResponseDTO<CaseSummaryDTO> getCases(@ModelAttribute CaseFilterDTO filter) {
        return caseService.getCases(filter);
    }

    @Operation(
            summary = "Listar casos de un proyecto",
            description = "Devuelve todos los casos de un proyecto con estado, progreso y alerta de vencimiento. " +
                    "Accesible para PROJECT_ADMIN y SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de casos del proyecto"),
                    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/api/v1/projects/{projectId}/cases")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SYSTEM_ADMIN')")
    public List<CaseSummaryDTO> getCasesByProject(@PathVariable Long projectId) throws NotFoundException {
        return caseService.getCasesByProject(projectId);
    }

    @Operation(
            summary = "Ver detalle de caso",
            description = "Devuelve el detalle completo de un caso incluyendo pasos. " +
                    "PROJECT_ADMIN y SYSTEM_ADMIN acceden libremente; DEVELOPER solo si tiene un paso asignado en el caso.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Caso encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado"),
                    @ApiResponse(responseCode = "404", description = "Caso no encontrado")
            })
    @GetMapping("/api/v1/cases/{caseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SYSTEM_ADMIN', 'DEVELOPER')")
    public CaseDTO getCaseById(@PathVariable Long caseId) throws NotFoundException {
        return caseService.getCaseById(caseId);
    }

    @Operation(
            summary = "Ver mis casos asignados",
            description = "Retorna todos los casos donde el desarrollador autenticado tiene al menos un paso asignado. " +
                    "Solo accesible para DEVELOPER.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de casos asignados"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/api/v1/cases/my-assigned")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('DEVELOPER')")
    public List<CaseSummaryDTO> getMyAssignedCases() {
        return caseService.getMyAssignedCases();
    }

    @Operation(
            summary = "Casos próximos a vencer y vencidos (alertas)",
            description = """
                    Devuelve casos con fecha límite en los próximos 3 días o ya vencidos.
                    - PROJECT_ADMIN: casos de los proyectos que administra.
                    - DEVELOPER: casos donde tiene al menos un paso asignado.
                    Los casos completados o cancelados no se incluyen.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de alertas"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/api/v1/cases/alerts")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'DEVELOPER')")
    public List<CaseSummaryDTO> getCaseAlerts() {
        return caseService.getCaseAlerts();
    }

    @Operation(
            summary = "Cambiar fecha límite del caso",
            description = "Actualiza la fecha límite de un caso que no esté completado ni cancelado. " +
                    "Solo accesible para PROJECT_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fecha límite actualizada"),
                    @ApiResponse(responseCode = "400", description = "El caso está completado o cancelado"),
                    @ApiResponse(responseCode = "404", description = "Caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/api/v1/cases/{caseId}/due-date")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    public CaseDTO updateDueDate(@PathVariable Long caseId,
                                 @RequestBody @Valid UpdateDueDateDTO dto) throws NotFoundException {
        return caseService.updateDueDate(caseId, dto);
    }

    @Operation(
            summary = "Cancelar caso",
            description = "Cancela un caso activo indicando la razón. " +
                    "Solo accesible para PROJECT_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Caso cancelado"),
                    @ApiResponse(responseCode = "400", description = "El caso ya está cancelado o completado"),
                    @ApiResponse(responseCode = "404", description = "Caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/api/v1/cases/{caseId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    public CaseDTO cancelCase(@PathVariable Long caseId,
                               @RequestBody @Valid CancelCaseDTO dto) throws NotFoundException {
        return caseService.cancelCase(caseId, dto);
    }
}
