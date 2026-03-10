package edu.robertob.ayd2_p1_backend.reports.controllers;

import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.reports.models.dto.*;
import edu.robertob.ayd2_p1_backend.reports.services.ReportService;
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
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    // ── projectCaseCount ──────────────────────────────────────────────────────

    @Test
    void projectCaseCount_delegatesToService() {
        List<ProjectCaseCountDTO> expected = List.of(
                new ProjectCaseCountDTO(1L, "Proj A", "ACTIVE", 3L));
        when(reportService.projectCaseCount("ACTIVE")).thenReturn(expected);

        List<ProjectCaseCountDTO> result = reportController.projectCaseCount("ACTIVE");

        assertSame(expected, result);
        verify(reportService).projectCaseCount("ACTIVE");
    }

    @Test
    void projectCaseCount_noFilter_delegatesToService() {
        when(reportService.projectCaseCount(null)).thenReturn(List.of());

        reportController.projectCaseCount(null);

        verify(reportService).projectCaseCount(null);
    }

    @Test
    void projectCaseCount_propagatesBadRequest() {
        when(reportService.projectCaseCount("BAD")).thenThrow(new BadRequestException("invalid"));

        assertThrows(BadRequestException.class, () -> reportController.projectCaseCount("BAD"));
    }

    // ── hoursAndMoneyByProject ────────────────────────────────────────────────

    @Test
    void hoursAndMoneyByProject_delegatesToService() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(5L, 10.0, 500.0);
        when(reportService.hoursAndMoneyByProject(1L)).thenReturn(dto);

        HoursAndMoneyDTO result = reportController.hoursAndMoneyByProject(1L);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByProject_propagatesNotFound() throws NotFoundException {
        when(reportService.hoursAndMoneyByProject(99L)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> reportController.hoursAndMoneyByProject(99L));
    }

    // ── hoursAndMoneyByDeveloper ──────────────────────────────────────────────

    @Test
    void hoursAndMoneyByDeveloper_delegatesToService() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(3L, 6.0, 300.0);
        when(reportService.hoursAndMoneyByDeveloper(2L)).thenReturn(dto);

        HoursAndMoneyDTO result = reportController.hoursAndMoneyByDeveloper(2L);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByDeveloper_propagatesNotFound() throws NotFoundException {
        when(reportService.hoursAndMoneyByDeveloper(99L)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> reportController.hoursAndMoneyByDeveloper(99L));
    }

    // ── hoursAndMoneyByCaseType ───────────────────────────────────────────────

    @Test
    void hoursAndMoneyByCaseType_delegatesToService() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(2L, 4.0, 200.0);
        when(reportService.hoursAndMoneyByCaseType(3L)).thenReturn(dto);

        HoursAndMoneyDTO result = reportController.hoursAndMoneyByCaseType(3L);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByCaseType_propagatesNotFound() throws NotFoundException {
        when(reportService.hoursAndMoneyByCaseType(99L)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> reportController.hoursAndMoneyByCaseType(99L));
    }

    // ── hoursAndMoneyByDateRange ──────────────────────────────────────────────

    @Test
    void hoursAndMoneyByDateRange_delegatesToService() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(10L, 20.0, 1000.0);
        when(reportService.hoursAndMoneyByDateRange(from, to)).thenReturn(dto);

        HoursAndMoneyDTO result = reportController.hoursAndMoneyByDateRange(from, to);

        assertSame(dto, result);
    }

    @Test
    void hoursAndMoneyByDateRange_propagatesBadRequest() {
        Instant from = Instant.parse("2024-06-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-01T00:00:00Z");
        when(reportService.hoursAndMoneyByDateRange(from, to)).thenThrow(new BadRequestException("invalid range"));

        assertThrows(BadRequestException.class, () -> reportController.hoursAndMoneyByDateRange(from, to));
    }

    // ── developerReport ───────────────────────────────────────────────────────

    @Test
    void developerReport_delegatesToService() {
        List<DeveloperReportDTO> expected = List.of(
                new DeveloperReportDTO(1L, "Juan", "Perez", "juan", 50.0, 3L, 10.0, 500.0));
        when(reportService.developerReport(null)).thenReturn(expected);

        List<DeveloperReportDTO> result = reportController.developerReport(null);

        assertSame(expected, result);
    }

    @Test
    void developerReport_withSearch_delegatesToService() {
        when(reportService.developerReport("juan")).thenReturn(List.of());

        reportController.developerReport("juan");

        verify(reportService).developerReport("juan");
    }

    // ── projectReport ─────────────────────────────────────────────────────────

    @Test
    void projectReport_delegatesToService() {
        List<ProjectReportDTO> expected = List.of(
                new ProjectReportDTO(1L, "Proj A", "ACTIVE", 5L, 2L, 1L, 1L, 1L, 15.0, 750.0));
        when(reportService.projectReport(null, null)).thenReturn(expected);

        List<ProjectReportDTO> result = reportController.projectReport(null, null);

        assertSame(expected, result);
    }

    @Test
    void projectReport_withFilters_delegatesToService() {
        when(reportService.projectReport("ACTIVE", "proj")).thenReturn(List.of());

        reportController.projectReport("ACTIVE", "proj");

        verify(reportService).projectReport("ACTIVE", "proj");
    }

    @Test
    void projectReport_propagatesBadRequest() {
        when(reportService.projectReport("INVALID", null)).thenThrow(new BadRequestException("invalid"));

        assertThrows(BadRequestException.class, () -> reportController.projectReport("INVALID", null));
    }

    // ── developerWithMostCases ────────────────────────────────────────────────

    @Test
    void developerWithMostCases_delegatesToService() {
        TopDeveloperDTO top = new TopDeveloperDTO(1L, "Juan", "Perez", "juan", 10L, 500.0);
        when(reportService.developerWithMostCases()).thenReturn(Optional.of(top));

        Optional<TopDeveloperDTO> result = reportController.developerWithMostCases();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void developerWithMostCases_empty_returnsEmpty() {
        when(reportService.developerWithMostCases()).thenReturn(Optional.empty());

        assertTrue(reportController.developerWithMostCases().isEmpty());
    }

    // ── developerPaidTheMost ──────────────────────────────────────────────────

    @Test
    void developerPaidTheMost_delegatesToService() {
        TopDeveloperDTO top = new TopDeveloperDTO(2L, "Juan", "Perez", "juan", 5L, 2000.0);
        when(reportService.developerPaidTheMost()).thenReturn(Optional.of(top));

        Optional<TopDeveloperDTO> result = reportController.developerPaidTheMost();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void developerPaidTheMost_empty_returnsEmpty() {
        when(reportService.developerPaidTheMost()).thenReturn(Optional.empty());

        assertTrue(reportController.developerPaidTheMost().isEmpty());
    }

    // ── projectWithMostCompletedCases ─────────────────────────────────────────

    @Test
    void projectWithMostCompletedCases_delegatesToService() {
        TopProjectDTO top = new TopProjectDTO(1L, "Proj A", 15L);
        when(reportService.projectWithMostCompletedCases()).thenReturn(Optional.of(top));

        Optional<TopProjectDTO> result = reportController.projectWithMostCompletedCases();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void projectWithMostCompletedCases_empty_returnsEmpty() {
        when(reportService.projectWithMostCompletedCases()).thenReturn(Optional.empty());

        assertTrue(reportController.projectWithMostCompletedCases().isEmpty());
    }

    // ── projectWithMostCanceledCases ──────────────────────────────────────────

    @Test
    void projectWithMostCanceledCases_delegatesToService() {
        TopProjectDTO top = new TopProjectDTO(2L, "Proj B", 7L);
        when(reportService.projectWithMostCanceledCases()).thenReturn(Optional.of(top));

        Optional<TopProjectDTO> result = reportController.projectWithMostCanceledCases();

        assertTrue(result.isPresent());
        assertSame(top, result.get());
    }

    @Test
    void projectWithMostCanceledCases_empty_returnsEmpty() {
        when(reportService.projectWithMostCanceledCases()).thenReturn(Optional.empty());

        assertTrue(reportController.projectWithMostCanceledCases().isEmpty());
    }

    // ── casesByProject ────────────────────────────────────────────────────────

    @Test
    void casesByProject_delegatesToService() throws NotFoundException {
        List<CaseReportDTO> cases = List.of();
        when(reportService.casesByProject(1L)).thenReturn(cases);

        List<CaseReportDTO> result = reportController.casesByProject(1L);

        assertSame(cases, result);
    }

    @Test
    void casesByProject_propagatesNotFound() throws NotFoundException {
        when(reportService.casesByProject(99L)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> reportController.casesByProject(99L));
    }

    // ── casesByDeveloper ──────────────────────────────────────────────────────

    @Test
    void casesByDeveloper_delegatesToService() throws NotFoundException {
        List<CaseReportDTO> cases = List.of();
        when(reportService.casesByDeveloper(1L)).thenReturn(cases);

        List<CaseReportDTO> result = reportController.casesByDeveloper(1L);

        assertSame(cases, result);
    }

    @Test
    void casesByDeveloper_propagatesNotFound() throws NotFoundException {
        when(reportService.casesByDeveloper(99L)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> reportController.casesByDeveloper(99L));
    }

    // ── casesByCaseType ───────────────────────────────────────────────────────

    @Test
    void casesByCaseType_delegatesToService() throws NotFoundException {
        List<CaseReportDTO> cases = List.of();
        when(reportService.casesByCaseType(1L)).thenReturn(cases);

        List<CaseReportDTO> result = reportController.casesByCaseType(1L);

        assertSame(cases, result);
    }

    @Test
    void casesByCaseType_propagatesNotFound() throws NotFoundException {
        when(reportService.casesByCaseType(99L)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> reportController.casesByCaseType(99L));
    }
}
