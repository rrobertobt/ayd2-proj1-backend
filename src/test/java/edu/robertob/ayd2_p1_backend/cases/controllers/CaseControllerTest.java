package edu.robertob.ayd2_p1_backend.cases.controllers;

import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CancelCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CaseFilterDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.UpdateDueDateDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseSummaryDTO;
import edu.robertob.ayd2_p1_backend.cases.services.CaseService;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseControllerTest {

    @Mock
    private CaseService caseService;

    @InjectMocks
    private CaseController caseController;

    // ── createCase ────────────────────────────────────────────────────────────

    @Test
    void createCase_delegatesToServiceAndReturnsDTO() throws Exception {
        CreateCaseDTO dto = new CreateCaseDTO();
        dto.setProjectId(1L);
        dto.setCaseTypeId(2L);
        dto.setTitle("Error en login");
        dto.setDueDate(LocalDate.now().plusDays(10));

        CaseDTO expected = buildCaseDTO(1L, "Error en login");
        when(caseService.createCase(dto)).thenReturn(expected);

        CaseDTO result = caseController.createCase(dto);

        assertSame(expected, result);
        verify(caseService).createCase(dto);
    }

    @Test
    void createCase_propagatesNotFoundException() throws Exception {
        CreateCaseDTO dto = new CreateCaseDTO();
        when(caseService.createCase(dto)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseController.createCase(dto));
    }

    @Test
    void createCase_propagatesBadRequestException() throws Exception {
        CreateCaseDTO dto = new CreateCaseDTO();
        when(caseService.createCase(dto)).thenThrow(new BadRequestException("No stages"));

        assertThrows(BadRequestException.class, () -> caseController.createCase(dto));
    }

    // ── getCases ──────────────────────────────────────────────────────────────

    @Test
    void getCases_delegatesToServiceAndReturnsPagedResult() {
        CaseFilterDTO filter = new CaseFilterDTO();
        PagedResponseDTO<CaseSummaryDTO> expected = new PagedResponseDTO<>(
                List.of(buildSummaryDTO(1L, "Title")), 0, 10, 1L, 1, true);
        when(caseService.getCases(filter)).thenReturn(expected);

        PagedResponseDTO<CaseSummaryDTO> result = caseController.getCases(filter);

        assertSame(expected, result);
        verify(caseService).getCases(filter);
    }

    @Test
    void getCases_emptyPage_returnsEmptyContent() {
        CaseFilterDTO filter = new CaseFilterDTO();
        PagedResponseDTO<CaseSummaryDTO> expected = new PagedResponseDTO<>(List.of(), 0, 10, 0L, 0, true);
        when(caseService.getCases(filter)).thenReturn(expected);

        PagedResponseDTO<CaseSummaryDTO> result = caseController.getCases(filter);

        assertTrue(result.content().isEmpty());
    }

    @Test
    void getCases_propagatesBadRequestException() {
        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setStatus("INVALID");
        when(caseService.getCases(filter)).thenThrow(new BadRequestException("Estado inválido"));

        assertThrows(BadRequestException.class, () -> caseController.getCases(filter));
    }

    // ── getCasesByProject ─────────────────────────────────────────────────────

    @Test
    void getCasesByProject_delegatesToService() throws Exception {
        List<CaseSummaryDTO> expected = List.of(
                buildSummaryDTO(1L, "T1"),
                buildSummaryDTO(2L, "T2")
        );
        when(caseService.getCasesByProject(1L)).thenReturn(expected);

        List<CaseSummaryDTO> result = caseController.getCasesByProject(1L);

        assertSame(expected, result);
        verify(caseService).getCasesByProject(1L);
    }

    @Test
    void getCasesByProject_propagatesNotFoundException() throws Exception {
        when(caseService.getCasesByProject(99L)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseController.getCasesByProject(99L));
    }

    @Test
    void getCasesByProject_emptyProject_returnsEmptyList() throws Exception {
        when(caseService.getCasesByProject(1L)).thenReturn(List.of());

        List<CaseSummaryDTO> result = caseController.getCasesByProject(1L);

        assertTrue(result.isEmpty());
    }

    // ── getCaseById ───────────────────────────────────────────────────────────

    @Test
    void getCaseById_delegatesToService() throws Exception {
        CaseDTO expected = buildCaseDTO(1L, "Case");
        when(caseService.getCaseById(1L)).thenReturn(expected);

        CaseDTO result = caseController.getCaseById(1L);

        assertSame(expected, result);
        verify(caseService).getCaseById(1L);
    }

    @Test
    void getCaseById_propagatesNotFoundException() throws Exception {
        when(caseService.getCaseById(99L)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseController.getCaseById(99L));
    }

    // ── updateDueDate ─────────────────────────────────────────────────────────

    @Test
    void updateDueDate_delegatesToService() throws Exception {
        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(20));
        CaseDTO expected = buildCaseDTO(1L, "Case");
        when(caseService.updateDueDate(1L, dto)).thenReturn(expected);

        CaseDTO result = caseController.updateDueDate(1L, dto);

        assertSame(expected, result);
        verify(caseService).updateDueDate(1L, dto);
    }

    @Test
    void updateDueDate_propagatesNotFoundException() throws Exception {
        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(10));
        when(caseService.updateDueDate(eq(99L), any())).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseController.updateDueDate(99L, dto));
    }

    @Test
    void updateDueDate_propagatesBadRequestException() throws Exception {
        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(10));
        when(caseService.updateDueDate(eq(1L), any()))
                .thenThrow(new BadRequestException("Cannot update completed case"));

        assertThrows(BadRequestException.class, () -> caseController.updateDueDate(1L, dto));
    }

    // ── cancelCase ────────────────────────────────────────────────────────────

    @Test
    void cancelCase_delegatesToService() throws Exception {
        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Cliente canceló");
        CaseDTO expected = buildCaseDTO(1L, "Case");
        when(caseService.cancelCase(1L, dto)).thenReturn(expected);

        CaseDTO result = caseController.cancelCase(1L, dto);

        assertSame(expected, result);
        verify(caseService).cancelCase(1L, dto);
    }

    @Test
    void cancelCase_propagatesNotFoundException() throws Exception {
        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Razón");
        when(caseService.cancelCase(eq(99L), any())).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseController.cancelCase(99L, dto));
    }

    @Test
    void cancelCase_propagatesBadRequestException() throws Exception {
        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Razón");
        when(caseService.cancelCase(eq(1L), any()))
                .thenThrow(new BadRequestException("Already canceled"));

        assertThrows(BadRequestException.class, () -> caseController.cancelCase(1L, dto));
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private static CaseDTO buildCaseDTO(Long id, String title) {
        return new CaseDTO(
                id, 1L, "Proyecto", 2L, "Bug", 10L, "Juan Pérez",
                title, "description", "OPEN",
                LocalDate.now().plusDays(5), false, 0,
                null, null,
                Instant.now(), Instant.now(),
                List.of()
        );
    }

    private static CaseSummaryDTO buildSummaryDTO(Long id, String title) {
        return new CaseSummaryDTO(
                id, 1L, "Proyecto", 2L, "Bug",
                title, "OPEN",
                LocalDate.now().plusDays(5), false, 0,
                Instant.now(), Instant.now()
        );
    }
}
