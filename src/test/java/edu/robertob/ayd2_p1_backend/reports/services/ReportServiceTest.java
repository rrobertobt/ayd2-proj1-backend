package edu.robertob.ayd2_p1_backend.reports.services;

import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectRepository;
import edu.robertob.ayd2_p1_backend.reports.models.dto.*;
import edu.robertob.ayd2_p1_backend.reports.repositories.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CaseTypeRepository caseTypeRepository;

    @InjectMocks
    private ReportService reportService;

    // ── projectCaseCount ──────────────────────────────────────────────────────

    @Test
    void projectCaseCount_noFilter_returnsAll() {
        List<ProjectCaseCountDTO> expected = List.of(
                new ProjectCaseCountDTO(1L, "Proj A", "ACTIVE", 3L));
        when(reportRepository.projectCaseCount(null)).thenReturn(expected);

        List<ProjectCaseCountDTO> result = reportService.projectCaseCount(null);

        assertSame(expected, result);
        verify(reportRepository).projectCaseCount(null);
    }

    @Test
    void projectCaseCount_withBlankStatus_returnsAll() {
        List<ProjectCaseCountDTO> expected = List.of();
        when(reportRepository.projectCaseCount(null)).thenReturn(expected);

        List<ProjectCaseCountDTO> result = reportService.projectCaseCount("   ");

        assertSame(expected, result);
        verify(reportRepository).projectCaseCount(null);
    }

    @Test
    void projectCaseCount_withValidStatus_filtersResults() {
        List<ProjectCaseCountDTO> expected = List.of(
                new ProjectCaseCountDTO(1L, "Proj A", "ACTIVE", 3L));
        when(reportRepository.projectCaseCount("ACTIVE")).thenReturn(expected);

        List<ProjectCaseCountDTO> result = reportService.projectCaseCount("active");

        assertSame(expected, result);
        verify(reportRepository).projectCaseCount("ACTIVE");
    }

    @Test
    void projectCaseCount_withInvalidStatus_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> reportService.projectCaseCount("INVALID"));
        verify(reportRepository, never()).projectCaseCount(any());
    }

    // ── hoursAndMoneyByProject ────────────────────────────────────────────────

    @Test
    void hoursAndMoneyByProject_projectNotFound_throwsNotFound() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> reportService.hoursAndMoneyByProject(99L));
    }

    @Test
    void hoursAndMoneyByProject_returnsDto() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(5L, 10.0, 500.0);
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByProject(1L)).thenReturn(dto);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByProject(1L);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByProject_repositoryReturnsNull_returnsZeroDto() throws NotFoundException {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByProject(1L)).thenReturn(null);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByProject(1L);

        assertEquals(0L, result.totalCases());
        assertEquals(0.0, result.totalHours());
        assertEquals(0.0, result.totalMoney());
    }

    // ── hoursAndMoneyByDeveloper ──────────────────────────────────────────────

    @Test
    void hoursAndMoneyByDeveloper_employeeNotFound_throwsNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> reportService.hoursAndMoneyByDeveloper(99L));
    }

    @Test
    void hoursAndMoneyByDeveloper_returnsDto() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(3L, 6.0, 300.0);
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByDeveloper(1L)).thenReturn(dto);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByDeveloper(1L);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByDeveloper_repositoryReturnsNull_returnsZeroDto() throws NotFoundException {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByDeveloper(1L)).thenReturn(null);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByDeveloper(1L);

        assertEquals(0L, result.totalCases());
    }

    // ── hoursAndMoneyByCaseType ───────────────────────────────────────────────

    @Test
    void hoursAndMoneyByCaseType_notFound_throwsNotFound() {
        when(caseTypeRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> reportService.hoursAndMoneyByCaseType(99L));
    }

    @Test
    void hoursAndMoneyByCaseType_returnsDto() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(2L, 4.0, 200.0);
        when(caseTypeRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByCaseType(1L)).thenReturn(dto);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByCaseType(1L);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByCaseType_repositoryReturnsNull_returnsZeroDto() throws NotFoundException {
        when(caseTypeRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByCaseType(1L)).thenReturn(null);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByCaseType(1L);

        assertEquals(0L, result.totalCases());
    }

    // ── hoursAndMoneyByDateRange ──────────────────────────────────────────────

    @Test
    void hoursAndMoneyByDateRange_fromNull_throwsBadRequest() {
        Instant to = Instant.now();

        assertThrows(BadRequestException.class,
                () -> reportService.hoursAndMoneyByDateRange(null, to));
    }

    @Test
    void hoursAndMoneyByDateRange_toNull_throwsBadRequest() {
        Instant from = Instant.now();

        assertThrows(BadRequestException.class,
                () -> reportService.hoursAndMoneyByDateRange(from, null));
    }

    @Test
    void hoursAndMoneyByDateRange_fromAfterTo_throwsBadRequest() {
        Instant from = Instant.parse("2024-06-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-01T00:00:00Z");

        assertThrows(BadRequestException.class,
                () -> reportService.hoursAndMoneyByDateRange(from, to));
    }

    @Test
    void hoursAndMoneyByDateRange_validRange_returnsDto() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(10L, 20.0, 1000.0);
        when(reportRepository.hoursAndMoneyByDateRange(from, to)).thenReturn(dto);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByDateRange(from, to);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByDateRange_repositoryReturnsNull_returnsZeroDto() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        when(reportRepository.hoursAndMoneyByDateRange(from, to)).thenReturn(null);

        HoursAndMoneyDTO result = reportService.hoursAndMoneyByDateRange(from, to);

        assertEquals(0L, result.totalCases());
    }

    // ── developerReport ───────────────────────────────────────────────────────

    @Test
    void developerReport_nullSearch_passesNullToRepository() {
        when(reportRepository.developerReport(null)).thenReturn(List.of());

        reportService.developerReport(null);

        verify(reportRepository).developerReport(null);
    }

    @Test
    void developerReport_blankSearch_passesNullToRepository() {
        when(reportRepository.developerReport(null)).thenReturn(List.of());

        reportService.developerReport("   ");

        verify(reportRepository).developerReport(null);
    }

    @Test
    void developerReport_withSearch_passesTrimmedValue() {
        when(reportRepository.developerReport("john")).thenReturn(List.of());

        reportService.developerReport("  john  ");

        verify(reportRepository).developerReport("john");
    }

    // ── projectReport ─────────────────────────────────────────────────────────

    @Test
    void projectReport_noFilters_passesNullsToRepository() {
        when(reportRepository.projectReport(null, null)).thenReturn(List.of());

        reportService.projectReport(null, null);

        verify(reportRepository).projectReport(null, null);
    }

    @Test
    void projectReport_withInvalidStatus_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> reportService.projectReport("UNKNOWN", null));
    }

    @Test
    void projectReport_withValidStatusAndSearch_passesToRepository() {
        when(reportRepository.projectReport("ACTIVE", "proj")).thenReturn(List.of());

        reportService.projectReport("active", "  proj  ");

        verify(reportRepository).projectReport("ACTIVE", "proj");
    }

    // ── developerWithMostCases ────────────────────────────────────────────────

    @Test
    void developerWithMostCases_delegatesToRepository() {
        TopDeveloperDTO top = new TopDeveloperDTO(1L, "Alice", "Smith", "alice", 10L, 500.0);
        when(reportRepository.developerWithMostCases()).thenReturn(Optional.of(top));

        Optional<TopDeveloperDTO> result = reportService.developerWithMostCases();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void developerWithMostCases_empty_returnsEmpty() {
        when(reportRepository.developerWithMostCases()).thenReturn(Optional.empty());

        Optional<TopDeveloperDTO> result = reportService.developerWithMostCases();

        assertTrue(result.isEmpty());
    }

    // ── developerPaidTheMost ──────────────────────────────────────────────────

    @Test
    void developerPaidTheMost_delegatesToRepository() {
        TopDeveloperDTO top = new TopDeveloperDTO(2L, "Bob", "Jones", "bob", 5L, 2000.0);
        when(reportRepository.developerPaidTheMost()).thenReturn(Optional.of(top));

        Optional<TopDeveloperDTO> result = reportService.developerPaidTheMost();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void developerPaidTheMost_empty_returnsEmpty() {
        when(reportRepository.developerPaidTheMost()).thenReturn(Optional.empty());

        assertTrue(reportService.developerPaidTheMost().isEmpty());
    }

    // ── projectWithMostCompletedCases ─────────────────────────────────────────

    @Test
    void projectWithMostCompletedCases_delegatesToRepository() {
        TopProjectDTO top = new TopProjectDTO(1L, "Proj A", 15L);
        when(reportRepository.projectWithMostCompletedCases()).thenReturn(Optional.of(top));

        Optional<TopProjectDTO> result = reportService.projectWithMostCompletedCases();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void projectWithMostCompletedCases_empty_returnsEmpty() {
        when(reportRepository.projectWithMostCompletedCases()).thenReturn(Optional.empty());

        assertTrue(reportService.projectWithMostCompletedCases().isEmpty());
    }

    // ── projectWithMostCanceledCases ──────────────────────────────────────────

    @Test
    void projectWithMostCanceledCases_delegatesToRepository() {
        TopProjectDTO top = new TopProjectDTO(2L, "Proj B", 7L);
        when(reportRepository.projectWithMostCanceledCases()).thenReturn(Optional.of(top));

        Optional<TopProjectDTO> result = reportService.projectWithMostCanceledCases();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void projectWithMostCanceledCases_empty_returnsEmpty() {
        when(reportRepository.projectWithMostCanceledCases()).thenReturn(Optional.empty());

        assertTrue(reportService.projectWithMostCanceledCases().isEmpty());
    }

    // ── casesByProject ────────────────────────────────────────────────────────

    @Test
    void casesByProject_notFound_throwsNotFound() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> reportService.casesByProject(99L));
    }

    @Test
    void casesByProject_returnsListFromRepository() throws NotFoundException {
        List<CaseReportDTO> cases = List.of();
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.casesByProject(1L)).thenReturn(cases);

        List<CaseReportDTO> result = reportService.casesByProject(1L);

        assertSame(cases, result);
    }

    // ── casesByDeveloper ──────────────────────────────────────────────────────

    @Test
    void casesByDeveloper_notFound_throwsNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> reportService.casesByDeveloper(99L));
    }

    @Test
    void casesByDeveloper_returnsListFromRepository() throws NotFoundException {
        List<CaseReportDTO> cases = List.of();
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.casesByDeveloper(1L)).thenReturn(cases);

        List<CaseReportDTO> result = reportService.casesByDeveloper(1L);

        assertSame(cases, result);
    }

    // ── casesByCaseType ───────────────────────────────────────────────────────

    @Test
    void casesByCaseType_notFound_throwsNotFound() {
        when(caseTypeRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> reportService.casesByCaseType(99L));
    }

    @Test
    void casesByCaseType_returnsListFromRepository() throws NotFoundException {
        List<CaseReportDTO> cases = List.of();
        when(caseTypeRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.casesByCaseType(1L)).thenReturn(cases);

        List<CaseReportDTO> result = reportService.casesByCaseType(1L);

        assertSame(cases, result);
    }
}
