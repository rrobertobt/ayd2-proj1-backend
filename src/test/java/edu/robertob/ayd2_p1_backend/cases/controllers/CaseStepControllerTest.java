package edu.robertob.ayd2_p1_backend.cases.controllers;

import edu.robertob.ayd2_p1_backend.cases.models.dto.request.ApproveStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.AssignStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.RejectStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseStepDTO;
import edu.robertob.ayd2_p1_backend.cases.services.CaseStepService;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseStepControllerTest {

    @Mock
    private CaseStepService caseStepService;

    @InjectMocks
    private CaseStepController caseStepController;

    // ── getSteps ──────────────────────────────────────────────────────────────

    @Test
    void getSteps_delegatesToService() throws Exception {
        List<CaseStepDTO> expected = List.of(
                buildStepDTO(200L, 1, "PENDING"),
                buildStepDTO(201L, 2, "PENDING")
        );
        when(caseStepService.getSteps(1L)).thenReturn(expected);

        List<CaseStepDTO> result = caseStepController.getSteps(1L);

        assertSame(expected, result);
        verify(caseStepService).getSteps(1L);
    }

    @Test
    void getSteps_emptyCase_returnsEmptyList() throws Exception {
        when(caseStepService.getSteps(1L)).thenReturn(List.of());

        List<CaseStepDTO> result = caseStepController.getSteps(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getSteps_caseNotFound_propagatesNotFoundException() throws Exception {
        when(caseStepService.getSteps(99L)).thenThrow(new NotFoundException("Caso no encontrado"));

        assertThrows(NotFoundException.class, () -> caseStepController.getSteps(99L));
    }

    // ── assignStep ────────────────────────────────────────────────────────────

    @Test
    void assignStep_delegatesToService() throws Exception {
        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);
        CaseStepDTO expected = buildStepDTO(200L, 1, "ASSIGNED");
        when(caseStepService.assignStep(1L, 200L, dto)).thenReturn(expected);

        CaseStepDTO result = caseStepController.assignStep(1L, 200L, dto);

        assertSame(expected, result);
        verify(caseStepService).assignStep(1L, 200L, dto);
    }

    @Test
    void assignStep_caseNotFound_propagatesNotFoundException() throws Exception {
        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);
        when(caseStepService.assignStep(eq(99L), eq(200L), any()))
                .thenThrow(new NotFoundException("Caso no encontrado"));

        assertThrows(NotFoundException.class,
                () -> caseStepController.assignStep(99L, 200L, dto));
    }

    @Test
    void assignStep_employeeNotMember_propagatesBadRequestException() throws Exception {
        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);
        when(caseStepService.assignStep(eq(1L), eq(200L), any()))
                .thenThrow(new BadRequestException("No es miembro del proyecto"));

        assertThrows(BadRequestException.class,
                () -> caseStepController.assignStep(1L, 200L, dto));
    }

    @Test
    void assignStep_approvedStep_propagatesBadRequestException() throws Exception {
        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);
        when(caseStepService.assignStep(eq(1L), eq(200L), any()))
                .thenThrow(new BadRequestException("El paso ya fue aprobado"));

        assertThrows(BadRequestException.class,
                () -> caseStepController.assignStep(1L, 200L, dto));
    }

    // ── approveStep ───────────────────────────────────────────────────────────

    @Test
    void approveStep_delegatesToService() throws Exception {
        ApproveStepDTO dto = new ApproveStepDTO();
        dto.setNextAssigneeUserId(50L);
        CaseStepDTO expected = buildStepDTO(200L, 1, "APPROVED");
        when(caseStepService.approveStep(1L, 200L, dto)).thenReturn(expected);

        CaseStepDTO result = caseStepController.approveStep(1L, 200L, dto);

        assertSame(expected, result);
        verify(caseStepService).approveStep(1L, 200L, dto);
    }

    @Test
    void approveStep_withoutNextAssignee_delegatesToService() throws Exception {
        ApproveStepDTO dto = new ApproveStepDTO();
        CaseStepDTO expected = buildStepDTO(200L, 1, "APPROVED");
        when(caseStepService.approveStep(1L, 200L, dto)).thenReturn(expected);

        CaseStepDTO result = caseStepController.approveStep(1L, 200L, dto);

        assertSame(expected, result);
    }

    @Test
    void approveStep_caseNotFound_propagatesNotFoundException() throws Exception {
        when(caseStepService.approveStep(eq(99L), eq(200L), any()))
                .thenThrow(new NotFoundException("Caso no encontrado"));

        assertThrows(NotFoundException.class,
                () -> caseStepController.approveStep(99L, 200L, new ApproveStepDTO()));
    }

    @Test
    void approveStep_invalidStatus_propagatesBadRequestException() throws Exception {
        when(caseStepService.approveStep(eq(1L), eq(200L), any()))
                .thenThrow(new BadRequestException("El paso no puede ser aprobado"));

        assertThrows(BadRequestException.class,
                () -> caseStepController.approveStep(1L, 200L, new ApproveStepDTO()));
    }

    @Test
    void approveStep_nextAssigneeNotMember_propagatesBadRequestException() throws Exception {
        ApproveStepDTO dto = new ApproveStepDTO();
        dto.setNextAssigneeUserId(50L);
        when(caseStepService.approveStep(eq(1L), eq(200L), any()))
                .thenThrow(new BadRequestException("El siguiente desarrollador no es miembro"));

        assertThrows(BadRequestException.class,
                () -> caseStepController.approveStep(1L, 200L, dto));
    }

    // ── rejectStep ────────────────────────────────────────────────────────────

    @Test
    void rejectStep_delegatesToService() throws Exception {
        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Falta manejar edge cases");
        CaseStepDTO expected = buildStepDTO(200L, 1, "IN_PROGRESS");
        when(caseStepService.rejectStep(1L, 200L, dto)).thenReturn(expected);

        CaseStepDTO result = caseStepController.rejectStep(1L, 200L, dto);

        assertSame(expected, result);
        verify(caseStepService).rejectStep(1L, 200L, dto);
    }

    @Test
    void rejectStep_caseNotFound_propagatesNotFoundException() throws Exception {
        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Razón");
        when(caseStepService.rejectStep(eq(99L), eq(200L), any()))
                .thenThrow(new NotFoundException("Caso no encontrado"));

        assertThrows(NotFoundException.class,
                () -> caseStepController.rejectStep(99L, 200L, dto));
    }

    @Test
    void rejectStep_stepNotFound_propagatesNotFoundException() throws Exception {
        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Razón");
        when(caseStepService.rejectStep(eq(1L), eq(999L), any()))
                .thenThrow(new NotFoundException("Paso no encontrado"));

        assertThrows(NotFoundException.class,
                () -> caseStepController.rejectStep(1L, 999L, dto));
    }

    @Test
    void rejectStep_invalidStatus_propagatesBadRequestException() throws Exception {
        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Razón");
        when(caseStepService.rejectStep(eq(1L), eq(200L), any()))
                .thenThrow(new BadRequestException("El paso no puede ser rechazado"));

        assertThrows(BadRequestException.class,
                () -> caseStepController.rejectStep(1L, 200L, dto));
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private static CaseStepDTO buildStepDTO(Long id, int stepOrder, String status) {
        return new CaseStepDTO(
                id, 100L, "Stage " + stepOrder, stepOrder, status,
                null, null,
                null, null, null, null, null, null,
                Instant.now(), Instant.now()
        );
    }
}
