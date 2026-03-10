package edu.robertob.ayd2_p1_backend.cases.controllers;

import edu.robertob.ayd2_p1_backend.cases.models.dto.request.ApproveStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.AssignStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateWorklogDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.RejectStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.WorklogDTO;
import edu.robertob.ayd2_p1_backend.cases.services.CaseStepService;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
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
public class CaseStepController {

    private final CaseStepService caseStepService;

    @Operation(
            summary = "Listar pasos de un caso",
            description = "Retorna cada step con su estado, desarrollador asignado y timestamps. " +
                    "Accesible para PROJECT_ADMIN y DEVELOPER.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pasos"),
                    @ApiResponse(responseCode = "404", description = "Caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/api/v1/cases/{caseId}/steps")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'DEVELOPER')")
    public List<CaseStepDTO> getSteps(@PathVariable Long caseId) throws NotFoundException {
        return caseStepService.getSteps(caseId);
    }

    @Operation(
            summary = "Asignar desarrollador a un paso",
            description = """
                    Asigna un desarrollador miembro del proyecto a un paso.
                    El paso pasa a estado `ASSIGNED`.
                    Solo accesible para PROJECT_ADMIN.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paso asignado"),
                    @ApiResponse(responseCode = "400", description = "Empleado no es miembro del proyecto o paso ya aprobado"),
                    @ApiResponse(responseCode = "404", description = "Caso, paso o usuario no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/api/v1/cases/{caseId}/steps/{stepId}/assign")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    public CaseStepDTO assignStep(@PathVariable Long caseId,
                                   @PathVariable Long stepId,
                                   @RequestBody @Valid AssignStepDTO dto) throws NotFoundException {
        return caseStepService.assignStep(caseId, stepId, dto);
    }

    @Operation(
            summary = "Aprobar paso y asignar siguiente desarrollador",
            description = """
                    Aprueba el trabajo del paso actual.
                    - El paso pasa a `APPROVED`.
                    - Si hay siguiente paso, se asigna al desarrollador indicado en `nextAssigneeUserId`.
                    - Si no hay más pasos, el caso finaliza automáticamente (`COMPLETED`).
                    Solo accesible para PROJECT_ADMIN.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paso aprobado"),
                    @ApiResponse(responseCode = "400", description = "El paso no está en un estado aprobable"),
                    @ApiResponse(responseCode = "404", description = "Caso, paso o usuario no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/api/v1/cases/{caseId}/steps/{stepId}/approve")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    public CaseStepDTO approveStep(@PathVariable Long caseId,
                                    @PathVariable Long stepId,
                                    @RequestBody ApproveStepDTO dto) throws NotFoundException {
        return caseStepService.approveStep(caseId, stepId, dto);
    }

    @Operation(
            summary = "Rechazar paso",
            description = """
                    Rechaza el trabajo del paso.
                    - El paso vuelve a estado `IN_PROGRESS`.
                    - El desarrollador asignado debe registrar un nuevo worklog.
                    Solo accesible para PROJECT_ADMIN.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paso rechazado"),
                    @ApiResponse(responseCode = "400", description = "El paso no está en un estado rechazable"),
                    @ApiResponse(responseCode = "404", description = "Caso o paso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PatchMapping("/api/v1/cases/{caseId}/steps/{stepId}/reject")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    public CaseStepDTO rejectStep(@PathVariable Long caseId,
                                   @PathVariable Long stepId,
                                   @RequestBody @Valid RejectStepDTO dto) throws NotFoundException {
        return caseStepService.rejectStep(caseId, stepId, dto);
    }

    @Operation(
            summary = "Ver worklogs de un paso",
            description = """
                    Retorna todos los registros de trabajo de un paso, ordenados cronológicamente.
                    Útil para que el admin consulte el historial antes de aprobar o rechazar.
                    Accesible para PROJECT_ADMIN y DEVELOPER.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de worklogs"),
                    @ApiResponse(responseCode = "404", description = "Caso o paso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/api/v1/cases/{caseId}/steps/{stepId}/worklogs")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'DEVELOPER')")
    public List<WorklogDTO> getWorklogs(@PathVariable Long caseId,
                                        @PathVariable Long stepId) throws NotFoundException {
        return caseStepService.getWorklogs(caseId, stepId);
    }

    @Operation(
            summary = "Registrar trabajo en un paso",
            description = """
                    Registra horas trabajadas y un comentario en un paso.
                    - Solo el desarrollador asignado al paso puede registrar trabajo.
                    - El paso debe estar en estado `ASSIGNED` o `IN_PROGRESS`.
                    - Si el paso estaba en `ASSIGNED`, pasa automáticamente a `IN_PROGRESS`.
                    Solo accesible para DEVELOPER.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Worklog registrado"),
                    @ApiResponse(responseCode = "400", description = "Estado del paso inválido o caso terminado"),
                    @ApiResponse(responseCode = "403", description = "No es el desarrollador asignado"),
                    @ApiResponse(responseCode = "404", description = "Caso o paso no encontrado")
            })
    @PostMapping("/api/v1/cases/{caseId}/steps/{stepId}/worklogs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DEVELOPER')")
    public WorklogDTO createWorklog(@PathVariable Long caseId,
                                    @PathVariable Long stepId,
                                    @RequestBody @Valid CreateWorklogDTO dto) throws NotFoundException {
        return caseStepService.createWorklog(caseId, stepId, dto);
    }
}
