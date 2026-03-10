package edu.robertob.ayd2_p1_backend.casetypes.controllers;

import edu.robertob.ayd2_p1_backend.casetypes.models.dto.request.*;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeStageDTO;
import edu.robertob.ayd2_p1_backend.casetypes.services.CaseTypeService;
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
@RequestMapping("/api/v1/case-types")
@RequiredArgsConstructor
public class CaseTypeController {

    private final CaseTypeService caseTypeService;

    // ─────────────────────────────────────────────────────────────────────────
    // CASE TYPES
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Crear tipo de caso",
            description = "Crea un nuevo tipo de caso. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Tipo de caso creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public CaseTypeDTO createCaseType(@RequestBody @Valid CreateCaseTypeDTO dto) {
        return caseTypeService.createCaseType(dto);
    }

    @Operation(
            summary = "Listar tipos de caso (paginado y filtrado)",
            description = """
                    Devuelve una página de tipos de caso con sus etapas. Solo accesible para SYSTEM_ADMIN.

                    **Filtros disponibles (query params):**
                    - `search` – búsqueda parcial en nombre (case-insensitive)
                    - `active` – filtrar por estado: `true` | `false`

                    **Paginación:**
                    - `page` – número de página (inicia en 0, default: 0)
                    - `size` – tamaño de página (default: 10, máximo: 100)
                    - `sortBy` – campo de ordenamiento: `name` | `createdAt` (default: `createdAt`)
                    - `sortDir` – dirección: `asc` | `desc` (default: `desc`)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página de tipos de caso"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SYSTEM_ADMIN')")
    public PagedResponseDTO<CaseTypeDTO> getAllCaseTypes(@ModelAttribute CaseTypeFilterDTO filter) {
        return caseTypeService.getAllCaseTypes(filter);
    }

    @Operation(
            summary = "Ver detalle de tipo de caso",
            description = "Devuelve el detalle de un tipo de caso incluyendo sus etapas. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tipo de caso encontrado"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/{caseTypeId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public CaseTypeDTO getCaseTypeById(@PathVariable Long caseTypeId) throws NotFoundException {
        return caseTypeService.getCaseTypeById(caseTypeId);
    }

    @Operation(
            summary = "Editar tipo de caso",
            description = "Actualiza parcialmente el nombre y/o descripción de un tipo de caso. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tipo de caso actualizado"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/{caseTypeId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public CaseTypeDTO updateCaseType(@PathVariable Long caseTypeId,
                                      @RequestBody @Valid UpdateCaseTypeDTO dto) throws NotFoundException {
        return caseTypeService.updateCaseType(caseTypeId, dto);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGES
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Crear etapa del tipo de caso",
            description = "Agrega una nueva etapa al tipo de caso en el orden indicado. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Etapa creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Ya existe una etapa con ese orden"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PostMapping("/{caseTypeId}/stages")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public CaseTypeStageDTO createStage(@PathVariable Long caseTypeId,
                                        @RequestBody @Valid CreateCaseTypeStageDTO dto) throws NotFoundException {
        return caseTypeService.createStage(caseTypeId, dto);
    }

    @Operation(
            summary = "Listar etapas de un tipo de caso",
            description = "Devuelve las etapas del tipo de caso ordenadas por stageOrder. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de etapas"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/{caseTypeId}/stages")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<CaseTypeStageDTO> getStages(@PathVariable Long caseTypeId) throws NotFoundException {
        return caseTypeService.getStages(caseTypeId);
    }

    @Operation(
            summary = "Editar etapa",
            description = "Actualiza parcialmente el nombre, descripción y/o orden de una etapa. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Etapa actualizada"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso o etapa no encontrada"),
                    @ApiResponse(responseCode = "409", description = "Ya existe una etapa con ese orden"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/{caseTypeId}/stages/{stageId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public CaseTypeStageDTO updateStage(@PathVariable Long caseTypeId,
                                        @PathVariable Long stageId,
                                        @RequestBody @Valid UpdateCaseTypeStageDTO dto) throws NotFoundException {
        return caseTypeService.updateStage(caseTypeId, stageId, dto);
    }

    @Operation(
            summary = "Eliminar etapa",
            description = "Elimina una etapa del tipo de caso. Solo permitido si el tipo de caso no tiene casos activos asociados. Solo accesible para SYSTEM_ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Etapa eliminada"),
                    @ApiResponse(responseCode = "400", description = "El tipo de caso tiene casos activos"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso o etapa no encontrada"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @DeleteMapping("/{caseTypeId}/stages/{stageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public void deleteStage(@PathVariable Long caseTypeId,
                            @PathVariable Long stageId) throws NotFoundException {
        caseTypeService.deleteStage(caseTypeId, stageId);
    }
}
