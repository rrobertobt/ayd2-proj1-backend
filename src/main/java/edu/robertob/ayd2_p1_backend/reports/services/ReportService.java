package edu.robertob.ayd2_p1_backend.reports.services;

import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectRepository;
import edu.robertob.ayd2_p1_backend.reports.models.dto.*;
import edu.robertob.ayd2_p1_backend.reports.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final CaseTypeRepository caseTypeRepository;

    // ── 1. Project case count ─────────────────────────────────────────────────
    public List<ProjectCaseCountDTO> projectCaseCount(String status) {
        if (status != null && !status.isBlank()) {
            validateProjectStatus(status);
            return reportRepository.projectCaseCount(status.toUpperCase());
        }
        return reportRepository.projectCaseCount(null);
    }

    // ── 2. Hours & money by project ───────────────────────────────────────────
    public HoursAndMoneyDTO hoursAndMoneyByProject(Long projectId) throws NotFoundException {
        if (!projectRepository.existsById(projectId)) {
            throw new NotFoundException("Proyecto con id " + projectId + " no encontrado.");
        }
        return Optional.ofNullable(reportRepository.hoursAndMoneyByProject(projectId))
                .orElse(new HoursAndMoneyDTO(0L, 0.0, 0.0));
    }

    // ── 3. Hours & money by developer ────────────────────────────────────────
    public HoursAndMoneyDTO hoursAndMoneyByDeveloper(Long employeeId) throws NotFoundException {
        if (!employeeRepository.existsById(employeeId)) {
            throw new NotFoundException("Empleado con id " + employeeId + " no encontrado.");
        }
        return Optional.ofNullable(reportRepository.hoursAndMoneyByDeveloper(employeeId))
                .orElse(new HoursAndMoneyDTO(0L, 0.0, 0.0));
    }

    // ── 4. Hours & money by case type ────────────────────────────────────────
    public HoursAndMoneyDTO hoursAndMoneyByCaseType(Long caseTypeId) throws NotFoundException {
        if (!caseTypeRepository.existsById(caseTypeId)) {
            throw new NotFoundException("Tipo de caso con id " + caseTypeId + " no encontrado.");
        }
        return Optional.ofNullable(reportRepository.hoursAndMoneyByCaseType(caseTypeId))
                .orElse(new HoursAndMoneyDTO(0L, 0.0, 0.0));
    }

    // ── 5. Hours & money by date range ───────────────────────────────────────
    public HoursAndMoneyDTO hoursAndMoneyByDateRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("Se requieren los parámetros 'from' y 'to' (ISO-8601).");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' no puede ser posterior a 'to'.");
        }
        return Optional.ofNullable(reportRepository.hoursAndMoneyByDateRange(from, to))
                .orElse(new HoursAndMoneyDTO(0L, 0.0, 0.0));
    }

    // ── 6. Developer report ───────────────────────────────────────────────────
    public List<DeveloperReportDTO> developerReport(String search) {
        String param = (search != null && !search.isBlank()) ? search.trim() : null;
        return reportRepository.developerReport(param);
    }

    // ── 7. Project report ─────────────────────────────────────────────────────
    public List<ProjectReportDTO> projectReport(String status, String search) {
        if (status != null && !status.isBlank()) {
            validateProjectStatus(status);
        }
        String statusParam = (status != null && !status.isBlank()) ? status.toUpperCase() : null;
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return reportRepository.projectReport(statusParam, searchParam);
    }

    // ── 8. Developer with most cases ─────────────────────────────────────────
    public Optional<TopDeveloperDTO> developerWithMostCases() {
        return reportRepository.developerWithMostCases();
    }

    // ── 9. Developer paid the most ────────────────────────────────────────────
    public Optional<TopDeveloperDTO> developerPaidTheMost() {
        return reportRepository.developerPaidTheMost();
    }

    // ── 10. Project with most completed cases ─────────────────────────────────
    public Optional<TopProjectDTO> projectWithMostCompletedCases() {
        return reportRepository.projectWithMostCompletedCases();
    }

    // ── 11. Project with most canceled cases ──────────────────────────────────
    public Optional<TopProjectDTO> projectWithMostCanceledCases() {
        return reportRepository.projectWithMostCanceledCases();
    }

    // ── 12. Cases by project ──────────────────────────────────────────────────
    public List<CaseReportDTO> casesByProject(Long projectId) throws NotFoundException {
        if (!projectRepository.existsById(projectId)) {
            throw new NotFoundException("Proyecto con id " + projectId + " no encontrado.");
        }
        return reportRepository.casesByProject(projectId);
    }

    // ── 13. Cases by developer ────────────────────────────────────────────────
    public List<CaseReportDTO> casesByDeveloper(Long employeeId) throws NotFoundException {
        if (!employeeRepository.existsById(employeeId)) {
            throw new NotFoundException("Empleado con id " + employeeId + " no encontrado.");
        }
        return reportRepository.casesByDeveloper(employeeId);
    }

    // ── 14. Cases by case type ────────────────────────────────────────────────
    public List<CaseReportDTO> casesByCaseType(Long caseTypeId) throws NotFoundException {
        if (!caseTypeRepository.existsById(caseTypeId)) {
            throw new NotFoundException("Tipo de caso con id " + caseTypeId + " no encontrado.");
        }
        return reportRepository.casesByCaseType(caseTypeId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void validateProjectStatus(String status) {
        if (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("INACTIVE")) {
            throw new BadRequestException(
                    "Estado de proyecto inválido: '" + status + "'. Valores permitidos: ACTIVE, INACTIVE");
        }
    }
}
