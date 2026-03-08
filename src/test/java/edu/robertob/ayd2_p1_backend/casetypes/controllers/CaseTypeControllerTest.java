package edu.robertob.ayd2_p1_backend.casetypes.controllers;

import edu.robertob.ayd2_p1_backend.casetypes.models.dto.request.*;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeStageDTO;
import edu.robertob.ayd2_p1_backend.casetypes.services.CaseTypeService;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.DuplicateResourceException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseTypeControllerTest {

    @Mock
    private CaseTypeService caseTypeService;

    @InjectMocks
    private CaseTypeController caseTypeController;

    // ── createCaseType ────────────────────────────────────────────────────────

    @Test
    void createCaseType_delegatesToServiceAndReturnsDTO() {
        CreateCaseTypeDTO dto = new CreateCaseTypeDTO();
        dto.setName("Bug");
        CaseTypeDTO expected = buildCaseTypeDTO(1L, "Bug", true);
        when(caseTypeService.createCaseType(dto)).thenReturn(expected);

        CaseTypeDTO result = caseTypeController.createCaseType(dto);

        assertSame(expected, result);
        verify(caseTypeService).createCaseType(dto);
    }

    // ── getAllCaseTypes ────────────────────────────────────────────────────────

    @Test
    void getAllCaseTypes_delegatesToServiceAndReturnsList() {
        List<CaseTypeDTO> expected = List.of(
                buildCaseTypeDTO(1L, "Bug", true),
                buildCaseTypeDTO(2L, "Feature", true)
        );
        when(caseTypeService.getAllCaseTypes()).thenReturn(expected);

        List<CaseTypeDTO> result = caseTypeController.getAllCaseTypes();

        assertSame(expected, result);
        verify(caseTypeService).getAllCaseTypes();
    }

    @Test
    void getAllCaseTypes_emptyList_returnsEmpty() {
        when(caseTypeService.getAllCaseTypes()).thenReturn(List.of());

        List<CaseTypeDTO> result = caseTypeController.getAllCaseTypes();

        assertTrue(result.isEmpty());
    }

    // ── getCaseTypeById ───────────────────────────────────────────────────────

    @Test
    void getCaseTypeById_delegatesToService() throws NotFoundException {
        CaseTypeDTO expected = buildCaseTypeDTO(1L, "Bug", true);
        when(caseTypeService.getCaseTypeById(1L)).thenReturn(expected);

        CaseTypeDTO result = caseTypeController.getCaseTypeById(1L);

        assertSame(expected, result);
        verify(caseTypeService).getCaseTypeById(1L);
    }

    @Test
    void getCaseTypeById_propagatesNotFoundException() throws NotFoundException {
        when(caseTypeService.getCaseTypeById(99L)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseTypeController.getCaseTypeById(99L));
    }

    // ── updateCaseType ────────────────────────────────────────────────────────

    @Test
    void updateCaseType_delegatesToService() throws NotFoundException {
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName("Bug crítico");
        CaseTypeDTO expected = buildCaseTypeDTO(1L, "Bug crítico", true);
        when(caseTypeService.updateCaseType(1L, dto)).thenReturn(expected);

        CaseTypeDTO result = caseTypeController.updateCaseType(1L, dto);

        assertSame(expected, result);
        verify(caseTypeService).updateCaseType(1L, dto);
    }

    @Test
    void updateCaseType_propagatesNotFoundException() throws NotFoundException {
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        when(caseTypeService.updateCaseType(eq(99L), any())).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseTypeController.updateCaseType(99L, dto));
    }

    // ── createStage ───────────────────────────────────────────────────────────

    @Test
    void createStage_delegatesToService() throws NotFoundException {
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Desarrollo");
        dto.setStageOrder(2);
        CaseTypeStageDTO expected = buildStageDTO(10L, "Desarrollo", 2);
        when(caseTypeService.createStage(1L, dto)).thenReturn(expected);

        CaseTypeStageDTO result = caseTypeController.createStage(1L, dto);

        assertSame(expected, result);
        verify(caseTypeService).createStage(1L, dto);
    }

    @Test
    void createStage_propagatesNotFoundException() throws NotFoundException {
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Stage");
        dto.setStageOrder(1);
        when(caseTypeService.createStage(eq(99L), any())).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseTypeController.createStage(99L, dto));
    }

    @Test
    void createStage_propagatesDuplicateResourceException() throws NotFoundException {
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Análisis");
        dto.setStageOrder(1);
        when(caseTypeService.createStage(eq(1L), any()))
                .thenThrow(new DuplicateResourceException("Duplicate order"));

        assertThrows(DuplicateResourceException.class, () -> caseTypeController.createStage(1L, dto));
    }

    // ── getStages ─────────────────────────────────────────────────────────────

    @Test
    void getStages_delegatesToService() throws NotFoundException {
        List<CaseTypeStageDTO> expected = List.of(
                buildStageDTO(10L, "Análisis", 1),
                buildStageDTO(11L, "Desarrollo", 2)
        );
        when(caseTypeService.getStages(1L)).thenReturn(expected);

        List<CaseTypeStageDTO> result = caseTypeController.getStages(1L);

        assertSame(expected, result);
        verify(caseTypeService).getStages(1L);
    }

    @Test
    void getStages_propagatesNotFoundException() throws NotFoundException {
        when(caseTypeService.getStages(99L)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseTypeController.getStages(99L));
    }

    // ── updateStage ───────────────────────────────────────────────────────────

    @Test
    void updateStage_delegatesToService() throws NotFoundException {
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setName("Desarrollo y pruebas");
        dto.setStageOrder(2);
        CaseTypeStageDTO expected = buildStageDTO(10L, "Desarrollo y pruebas", 2);
        when(caseTypeService.updateStage(1L, 10L, dto)).thenReturn(expected);

        CaseTypeStageDTO result = caseTypeController.updateStage(1L, 10L, dto);

        assertSame(expected, result);
        verify(caseTypeService).updateStage(1L, 10L, dto);
    }

    @Test
    void updateStage_propagatesNotFoundException() throws NotFoundException {
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        when(caseTypeService.updateStage(eq(1L), eq(99L), any()))
                .thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> caseTypeController.updateStage(1L, 99L, dto));
    }

    @Test
    void updateStage_propagatesDuplicateResourceException() throws NotFoundException {
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setStageOrder(2);
        when(caseTypeService.updateStage(eq(1L), eq(10L), any()))
                .thenThrow(new DuplicateResourceException("Duplicate order"));

        assertThrows(DuplicateResourceException.class,
                () -> caseTypeController.updateStage(1L, 10L, dto));
    }

    // ── deleteStage ───────────────────────────────────────────────────────────

    @Test
    void deleteStage_delegatesToService() throws NotFoundException {
        doNothing().when(caseTypeService).deleteStage(1L, 10L);

        assertDoesNotThrow(() -> caseTypeController.deleteStage(1L, 10L));

        verify(caseTypeService).deleteStage(1L, 10L);
    }

    @Test
    void deleteStage_propagatesNotFoundException() throws NotFoundException {
        doThrow(new NotFoundException("Not found")).when(caseTypeService).deleteStage(99L, 10L);

        assertThrows(NotFoundException.class, () -> caseTypeController.deleteStage(99L, 10L));
    }

    @Test
    void deleteStage_propagatesBadRequestException() throws NotFoundException {
        doThrow(new BadRequestException("Active cases exist"))
                .when(caseTypeService).deleteStage(1L, 10L);

        assertThrows(BadRequestException.class, () -> caseTypeController.deleteStage(1L, 10L));
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private static CaseTypeDTO buildCaseTypeDTO(Long id, String name, boolean active) {
        return new CaseTypeDTO(id, name, "description", active, Instant.now(), Instant.now(), List.of());
    }

    private static CaseTypeStageDTO buildStageDTO(Long id, String name, int stageOrder) {
        return new CaseTypeStageDTO(id, name, null, stageOrder, true, Instant.now(), Instant.now());
    }
}
