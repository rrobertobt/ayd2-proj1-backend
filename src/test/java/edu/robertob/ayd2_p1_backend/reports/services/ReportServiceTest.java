package edu.robertob.ayd2_p1_backend.reports.services;

import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectRepository;
import edu.robertob.ayd2_p1_backend.reports.models.dto.HoursAndMoneyDTO;
import edu.robertob.ayd2_p1_backend.reports.models.dto.ProjectCaseCountDTO;
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

    @Mock ReportRepository reportRepository;
    @Mock ProjectRepository projectRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock CaseTypeRepository caseTypeRepository;

    @InjectMocks ReportService service;

    // ── projectCaseCount ──────────────────────────────────────────────────────

    @Test
    void projectCaseCount_noFilter_callsRepoWithNull() {
        when(reportRepository.projectCaseCount(null)).thenReturn(List.of());

        var result = service.projectCaseCount(null);

        assertTrue(result.isEmpty());
        verify(reportRepository).projectCaseCount(null);
    }

    @Test
    void projectCaseCount_validStatus_passesUpperCase() {
        when(reportRepository.projectCaseCount("ACTIVE")).thenReturn(
                List.of(new ProjectCaseCountDTO(1L, "Project A", "ACTIVE", 3L)));

        var result = service.projectCaseCount("active");

        assertEquals(1, result.size());
        verify(reportRepository).projectCaseCount("ACTIVE");
    }

    @Test
    void projectCaseCount_invalidStatus_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.projectCaseCount("UNKNOWN"));
        verify(reportRepository, never()).projectCaseCount(any());
    }

    // ── hoursAndMoneyByProject ────────────────────────────────────────────────

    @Test
    void hoursAndMoneyByProject_throwsWhenNotFound() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.hoursAndMoneyByProject(1L));
    }

    @Test
    void hoursAndMoneyByProject_returnsRepoResult() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(5L, 10.0, 200.0);
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByProject(1L)).thenReturn(dto);

        assertSame(dto, service.hoursAndMoneyByProject(1L));
    }

    @Test
    void hoursAndMoneyByProject_returnsZeroWhenRepoNull() throws NotFoundException {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByProject(1L)).thenReturn(null);

        var result = service.hoursAndMoneyByProject(1L);

        assertEquals(0L, result.totalCases());
        assertEquals(0.0, result.totalHours());
    }

    // ── hoursAndMoneyByDeveloper ──────────────────────────────────────────────

    @Test
    void hoursAndMoneyByDeveloper_throwsWhenNotFound() {
        when(employeeRepository.existsById(5L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.hoursAndMoneyByDeveloper(5L));
    }

    @Test
    void hoursAndMoneyByDeveloper_returnsRepoResult() throws NotFoundException {
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(3L, 6.0, 120.0);
        when(employeeRepository.existsById(5L)).thenReturn(true);
        when(reportRepository.hoursAndMoneyByDeveloper(5L)).thenReturn(dto);

        assertSame(dto, service.hoursAndMoneyByDeveloper(5L));
    }

    // ── hoursAndMoneyByDateRange ──────────────────────────────────────────────

    @Test
    void hoursAndMoneyByDateRange_throwsWhenFromNull() {
        assertThrows(BadRequestException.class,
                () -> service.hoursAndMoneyByDateRange(null, Instant.now()));
    }

    @Test
    void hoursAndMoneyByDateRange_throwsWhenFromAfterTo() {
        Instant now = Instant.now();
        assertThrows(BadRequestException.class,
                () -> service.hoursAndMoneyByDateRange(now.plusSeconds(60), now));
    }

    @Test
    void hoursAndMoneyByDateRange_success() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        HoursAndMoneyDTO dto = new HoursAndMoneyDTO(2L, 4.0, 80.0);
        when(reportRepository.hoursAndMoneyByDateRange(from, to)).thenReturn(dto);

        assertSame(dto, service.hoursAndMoneyByDateRange(from, to));
    }

    // ── casesByProject ────────────────────────────────────────────────────────

    @Test
    void casesByProject_throwsWhenNotFound() {
        when(projectRepository.existsById(9L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.casesByProject(9L));
    }

    @Test
    void casesByProject_returnsRepoResult() throws NotFoundException {
        when(projectRepository.existsById(9L)).thenReturn(true);
        when(reportRepository.casesByProject(9L)).thenReturn(List.of());

        var result = service.casesByProject(9L);

        assertNotNull(result);
        verify(reportRepository).casesByProject(9L);
    }

    // ── casesByDeveloper ──────────────────────────────────────────────────────

    @Test
    void casesByDeveloper_throwsWhenNotFound() {
        when(employeeRepository.existsById(2L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.casesByDeveloper(2L));
    }

    // ── developerWithMostCases ────────────────────────────────────────────────

    @Test
    void developerWithMostCases_delegatesToRepo() {
        when(reportRepository.developerWithMostCases()).thenReturn(Optional.empty());

        assertTrue(service.developerWithMostCases().isEmpty());
        verify(reportRepository).developerWithMostCases();
    }

    // ── projectWithMostCompletedCases ────────────────────────────────────────

    @Test
    void projectWithMostCompletedCases_delegatesToRepo() {
        when(reportRepository.projectWithMostCompletedCases()).thenReturn(Optional.empty());

        assertTrue(service.projectWithMostCompletedCases().isEmpty());
        verify(reportRepository).projectWithMostCompletedCases();
    }
}
