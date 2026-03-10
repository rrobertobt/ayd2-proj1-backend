package edu.robertob.ayd2_p1_backend.reports.controllers;

import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.reports.models.dto.*;
import edu.robertob.ayd2_p1_backend.reports.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    // ── 1. Project case count ─────────────────────────────────────────────────
    @Operation(
            summary = "Reporte de cantidad de casos por proyecto",
            description = """
                    Devuelve todos los proyectos con la cantidad de casos asociados.
                    Filtro opcional: `status` = `ACTIVE` | `INACTIVE`.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "400", description = "Estado inválido"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/projects/case-count")
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectCaseCountDTO> projectCaseCount(
            @Parameter(description = "Filtrar por estado del proyecto: ACTIVE | INACTIVE")
            @RequestParam(required = false) String status) {
        return reportService.projectCaseCount(status);
    }

    // ── 2. Hours & money by project ───────────────────────────────────────────
    @Operation(
            summary = "Horas y dinero invertido por proyecto",
            description = "Devuelve el total de horas y dinero invertido en la resolución de casos de un proyecto.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/projects/{projectId}/investment")
    @ResponseStatus(HttpStatus.OK)
    public HoursAndMoneyDTO hoursAndMoneyByProject(@PathVariable Long projectId) throws NotFoundException {
        return reportService.hoursAndMoneyByProject(projectId);
    }

    // ── 3. Hours & money by developer ────────────────────────────────────────
    @Operation(
            summary = "Horas y dinero invertido por desarrollador",
            description = "Devuelve el total de horas y dinero pagado a un desarrollador específico.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "404", description = "Empleado no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/developers/{employeeId}/investment")
    @ResponseStatus(HttpStatus.OK)
    public HoursAndMoneyDTO hoursAndMoneyByDeveloper(@PathVariable Long employeeId) throws NotFoundException {
        return reportService.hoursAndMoneyByDeveloper(employeeId);
    }

    // ── 4. Hours & money by case type ────────────────────────────────────────
    @Operation(
            summary = "Horas y dinero invertido por tipo de caso",
            description = "Devuelve el total de horas y dinero invertido en casos de un tipo específico.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/case-types/{caseTypeId}/investment")
    @ResponseStatus(HttpStatus.OK)
    public HoursAndMoneyDTO hoursAndMoneyByCaseType(@PathVariable Long caseTypeId) throws NotFoundException {
        return reportService.hoursAndMoneyByCaseType(caseTypeId);
    }

    // ── 5. Hours & money by date range ───────────────────────────────────────
    @Operation(
            summary = "Horas y dinero invertido en un intervalo de tiempo",
            description = """
                    Devuelve el total de horas y dinero invertido en casos dentro de un rango de fechas.
                    Los parámetros `from` y `to` deben estar en formato ISO-8601
                    (e.g. `2024-01-01T00:00:00Z`).
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/investment/by-date")
    @ResponseStatus(HttpStatus.OK)
    public HoursAndMoneyDTO hoursAndMoneyByDateRange(
            @Parameter(description = "Inicio del intervalo (ISO-8601, e.g. 2024-01-01T00:00:00Z)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Fin del intervalo (ISO-8601, e.g. 2024-12-31T23:59:59Z)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportService.hoursAndMoneyByDateRange(from, to);
    }

    // ── 6. Developer report ───────────────────────────────────────────────────
    @Operation(
            summary = "Reporte de desarrolladores",
            description = """
                    Lista todos los desarrolladores con horas y dinero total.
                    Filtro opcional: `search` (búsqueda por nombre, apellido o username).
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/developers")
    @ResponseStatus(HttpStatus.OK)
    public List<DeveloperReportDTO> developerReport(
            @Parameter(description = "Búsqueda por nombre, apellido o username")
            @RequestParam(required = false) String search) {
        return reportService.developerReport(search);
    }

    // ── 7. Project report ─────────────────────────────────────────────────────
    @Operation(
            summary = "Reporte de proyectos",
            description = """
                    Lista todos los proyectos con conteo de casos por estado, horas y dinero total.
                    Filtros opcionales: `status` (ACTIVE | INACTIVE), `search` (búsqueda por nombre).
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "400", description = "Estado inválido"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/projects")
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectReportDTO> projectReport(
            @Parameter(description = "Filtrar por estado: ACTIVE | INACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Búsqueda por nombre de proyecto")
            @RequestParam(required = false) String search) {
        return reportService.projectReport(status, search);
    }

    // ── 8. Developer with most cases ─────────────────────────────────────────
    @Operation(
            summary = "Desarrollador con más casos",
            description = "Devuelve el desarrollador que ha participado en el mayor número de casos.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado encontrado o vacío"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/developers/top/most-cases")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TopDeveloperDTO> developerWithMostCases() {
        return reportService.developerWithMostCases();
    }

    // ── 9. Developer paid the most ────────────────────────────────────────────
    @Operation(
            summary = "Desarrollador al que se le ha pagado más",
            description = "Devuelve el desarrollador al que se le ha pagado más dinero por atender casos.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado encontrado o vacío"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/developers/top/highest-paid")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TopDeveloperDTO> developerPaidTheMost() {
        return reportService.developerPaidTheMost();
    }

    // ── 10. Project with most completed cases ─────────────────────────────────
    @Operation(
            summary = "Proyecto con más casos finalizados",
            description = "Devuelve el proyecto que tiene más casos con estado COMPLETED.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado encontrado o vacío"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/projects/top/most-completed")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TopProjectDTO> projectWithMostCompletedCases() {
        return reportService.projectWithMostCompletedCases();
    }

    // ── 11. Project with most canceled cases ──────────────────────────────────
    @Operation(
            summary = "Proyecto con más casos cancelados",
            description = "Devuelve el proyecto que tiene más casos con estado CANCELED.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado encontrado o vacío"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/projects/top/most-canceled")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TopProjectDTO> projectWithMostCanceledCases() {
        return reportService.projectWithMostCanceledCases();
    }

    // ── 12. Cases by project ──────────────────────────────────────────────────
    @Operation(
            summary = "Reporte de casos por proyecto",
            description = "Lista todos los casos de un proyecto con horas y dinero total por caso.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/projects/{projectId}/cases")
    @ResponseStatus(HttpStatus.OK)
    public List<CaseReportDTO> casesByProject(@PathVariable Long projectId) throws NotFoundException {
        return reportService.casesByProject(projectId);
    }

    // ── 13. Cases by developer ────────────────────────────────────────────────
    @Operation(
            summary = "Reporte de casos por desarrollador",
            description = "Lista todos los casos en los que un desarrollador específico ha participado.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "404", description = "Empleado no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/developers/{employeeId}/cases")
    @ResponseStatus(HttpStatus.OK)
    public List<CaseReportDTO> casesByDeveloper(@PathVariable Long employeeId) throws NotFoundException {
        return reportService.casesByDeveloper(employeeId);
    }

    // ── 14. Cases by case type ────────────────────────────────────────────────
    @Operation(
            summary = "Reporte de casos por tipo de caso",
            description = "Lista todos los casos de un tipo específico con horas y dinero total.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte generado"),
                    @ApiResponse(responseCode = "404", description = "Tipo de caso no encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @GetMapping("/case-types/{caseTypeId}/cases")
    @ResponseStatus(HttpStatus.OK)
    public List<CaseReportDTO> casesByCaseType(@PathVariable Long caseTypeId) throws NotFoundException {
        return reportService.casesByCaseType(caseTypeId);
    }
}
