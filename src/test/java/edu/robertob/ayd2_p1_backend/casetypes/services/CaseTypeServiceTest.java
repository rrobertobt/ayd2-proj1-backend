package edu.robertob.ayd2_p1_backend.casetypes.services;

import edu.robertob.ayd2_p1_backend.casetypes.models.dto.request.*;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeStageDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeStageRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.DuplicateResourceException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseTypeServiceTest {

    @Mock private CaseTypeRepository caseTypeRepository;
    @Mock private CaseTypeStageRepository stageRepository;
    @InjectMocks private CaseTypeService caseTypeService;

    // ── helpers ───────────────────────────────────────────────────────────────

    private static CaseTypeModel ct(Long id, String name) {
        CaseTypeModel m = new CaseTypeModel();
        m.setId(id); m.setName(name); m.setDescription("desc"); m.setActive(true);
        return m;
    }

    private static CaseTypeStageModel stage(Long id, CaseTypeModel ct, int order) {
        CaseTypeStageModel s = new CaseTypeStageModel();
        s.setId(id); s.setCaseType(ct); s.setName("Stage " + order);
        s.setStageOrder(order); s.setActive(true);
        return s;
    }

    // ── createCaseType ────────────────────────────────────────────────────────

    @Test
    void createCaseType_savesAndReturnsDTO() {
        CreateCaseTypeDTO dto = new CreateCaseTypeDTO();
        dto.setName("Bug"); dto.setDescription("Bug type");
        CaseTypeModel saved = ct(1L, "Bug");
        when(caseTypeRepository.save(any())).thenReturn(saved);

        CaseTypeDTO result = caseTypeService.createCaseType(dto);

        assertEquals("Bug", result.name());
        assertTrue(result.active());
        assertTrue(result.stages().isEmpty());
    }

    // ── getAllCaseTypes ───────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getAllCaseTypes_returnsPagedResult() {
        CaseTypeFilterDTO f = new CaseTypeFilterDTO();
        CaseTypeModel m = ct(1L, "Bug");
        when(caseTypeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(m)));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        PagedResponseDTO<CaseTypeDTO> result = caseTypeService.getAllCaseTypes(f);

        assertEquals(1, result.content().size());
        assertEquals("Bug", result.content().get(0).name());
    }

    // ── getCaseTypeById ───────────────────────────────────────────────────────

    @Test
    void getCaseTypeById_found_returnsDTO() throws NotFoundException {
        CaseTypeModel m = ct(1L, "Bug");
        CaseTypeStageModel s = stage(10L, m, 1);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of(s));

        CaseTypeDTO result = caseTypeService.getCaseTypeById(1L);
        assertEquals("Bug", result.name());
        assertEquals(1, result.stages().size());
    }

    @Test
    void getCaseTypeById_notFound_throws() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> caseTypeService.getCaseTypeById(99L));
    }

    // ── updateCaseType ────────────────────────────────────────────────────────

    @Test
    void updateCaseType_updatesFields() throws NotFoundException {
        CaseTypeModel m = ct(1L, "Old");
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName("New"); dto.setDescription("NewDesc");
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(caseTypeRepository.save(m)).thenReturn(m);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        CaseTypeDTO result = caseTypeService.updateCaseType(1L, dto);
        assertEquals("New", result.name());
    }

    @Test
    void updateCaseType_emptyName_nameUnchanged() throws NotFoundException {
        CaseTypeModel m = ct(1L, "Keep");
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName(""); dto.setDescription("NewDesc");
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(caseTypeRepository.save(m)).thenReturn(m);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        caseTypeService.updateCaseType(1L, dto);
        assertEquals("Keep", m.getName());
    }

    @Test
    void updateCaseType_notFound_throws() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> caseTypeService.updateCaseType(99L, new UpdateCaseTypeDTO()));
    }

    // ── createStage ───────────────────────────────────────────────────────────

    @Test
    void createStage_valid_returnsStageDTO() throws NotFoundException {
        CaseTypeModel m = ct(1L, "Bug");
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Review"); dto.setStageOrder(1);
        CaseTypeStageModel saved = stage(5L, m, 1);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.existsByCaseTypeIdAndStageOrder(1L, 1)).thenReturn(false);
        when(stageRepository.save(any())).thenReturn(saved);

        CaseTypeStageDTO result = caseTypeService.createStage(1L, dto);
        assertEquals("Stage 1", result.name());
    }

    @Test
    void createStage_duplicateOrder_throwsDuplicate() {
        CaseTypeModel m = ct(1L, "Bug");
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Review"); dto.setStageOrder(1);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.existsByCaseTypeIdAndStageOrder(1L, 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> caseTypeService.createStage(1L, dto));
    }

    @Test
    void createStage_caseTypeNotFound_throws() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> caseTypeService.createStage(99L, new CreateCaseTypeStageDTO()));
    }

    // ── getStages ─────────────────────────────────────────────────────────────

    @Test
    void getStages_returnsList() throws NotFoundException {
        CaseTypeModel m = ct(1L, "Bug");
        CaseTypeStageModel s = stage(10L, m, 1);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of(s));

        List<CaseTypeStageDTO> result = caseTypeService.getStages(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getStages_caseTypeNotFound_throws() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> caseTypeService.getStages(99L));
    }

    // ── updateStage ───────────────────────────────────────────────────────────

    @Test
    void updateStage_updatesFields() throws NotFoundException {
        CaseTypeModel m = ct(1L, "Bug");
        CaseTypeStageModel s = stage(10L, m, 1);
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setName("Updated"); dto.setStageOrder(2);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(s));
        when(stageRepository.existsByCaseTypeIdAndStageOrderAndIdNot(1L, 2, 10L)).thenReturn(false);
        when(stageRepository.save(s)).thenReturn(s);

        CaseTypeStageDTO result = caseTypeService.updateStage(1L, 10L, dto);
        assertEquals("Updated", result.name());
        assertEquals(2, result.stageOrder());
    }

    @Test
    void updateStage_duplicateOrder_throwsDuplicate() {
        CaseTypeModel m = ct(1L, "Bug");
        CaseTypeStageModel s = stage(10L, m, 1);
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setStageOrder(2);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(s));
        when(stageRepository.existsByCaseTypeIdAndStageOrderAndIdNot(1L, 2, 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> caseTypeService.updateStage(1L, 10L, dto));
    }

    @Test
    void updateStage_stageNotFound_throws() {
        CaseTypeModel m = ct(1L, "Bug");
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByIdAndCaseTypeId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseTypeService.updateStage(1L, 99L, new UpdateCaseTypeStageDTO()));
    }

    // ── deleteStage ───────────────────────────────────────────────────────────

    @Test
    void deleteStage_valid_deletesStage() throws NotFoundException {
        CaseTypeModel m = ct(1L, "Bug");
        CaseTypeStageModel s = stage(10L, m, 1);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(s));
        when(caseTypeRepository.existsActiveCaseTicketsByCaseTypeId(1L)).thenReturn(false);

        caseTypeService.deleteStage(1L, 10L);
        verify(stageRepository).delete(s);
    }

    @Test
    void deleteStage_hasActiveCases_throwsBadRequest() {
        CaseTypeModel m = ct(1L, "Bug");
        CaseTypeStageModel s = stage(10L, m, 1);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(s));
        when(caseTypeRepository.existsActiveCaseTicketsByCaseTypeId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> caseTypeService.deleteStage(1L, 10L));
        verify(stageRepository, never()).delete(any());
    }

    @Test
    void deleteStage_stageNotFound_throws() {
        CaseTypeModel m = ct(1L, "Bug");
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(m));
        when(stageRepository.findByIdAndCaseTypeId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseTypeService.deleteStage(1L, 99L));
    }
}
