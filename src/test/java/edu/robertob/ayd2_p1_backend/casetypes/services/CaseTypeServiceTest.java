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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseTypeServiceTest {

    @Mock private CaseTypeRepository caseTypeRepository;
    @Mock private CaseTypeStageRepository stageRepository;

    @InjectMocks
    private CaseTypeService caseTypeService;

    // ── createCaseType ────────────────────────────────────────────────────────

    @Test
    void createCaseType_savesAndReturnsDTO() {
        CreateCaseTypeDTO dto = new CreateCaseTypeDTO();
        dto.setName("Bug");
        dto.setDescription("Errores en el sistema");

        CaseTypeModel saved = buildCaseType(1L, "Bug", "Errores en el sistema", true);
        when(caseTypeRepository.save(any(CaseTypeModel.class))).thenReturn(saved);

        CaseTypeDTO result = caseTypeService.createCaseType(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Bug", result.name());
        assertEquals("Errores en el sistema", result.description());
        assertTrue(result.active());
        assertTrue(result.stages().isEmpty());
        verify(caseTypeRepository).save(any(CaseTypeModel.class));
    }

    @Test
    void createCaseType_setsActiveTrueByDefault() {
        CreateCaseTypeDTO dto = new CreateCaseTypeDTO();
        dto.setName("Feature");
        dto.setDescription(null);

        ArgumentCaptor<CaseTypeModel> captor = ArgumentCaptor.forClass(CaseTypeModel.class);
        CaseTypeModel saved = buildCaseType(2L, "Feature", null, true);
        when(caseTypeRepository.save(captor.capture())).thenReturn(saved);

        caseTypeService.createCaseType(dto);

        assertTrue(captor.getValue().isActive());
    }

    @Test
    void createCaseType_withNullDescription_savesWithoutError() {
        CreateCaseTypeDTO dto = new CreateCaseTypeDTO();
        dto.setName("Improvement");
        dto.setDescription(null);

        CaseTypeModel saved = buildCaseType(3L, "Improvement", null, true);
        when(caseTypeRepository.save(any(CaseTypeModel.class))).thenReturn(saved);

        CaseTypeDTO result = caseTypeService.createCaseType(dto);

        assertNull(result.description());
    }

    // ── getAllCaseTypes ────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getAllCaseTypes_returnsPagedResultWithStages() {
        CaseTypeModel ct1 = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeModel ct2 = buildCaseType(2L, "Feature", null, true);
        CaseTypeStageModel stage = buildStage(10L, ct1, "Análisis", 1);
        Page<CaseTypeModel> mockPage = new PageImpl<>(List.of(ct1, ct2));

        when(caseTypeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of(stage));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(2L)).thenReturn(List.of());

        PagedResponseDTO<CaseTypeDTO> result = caseTypeService.getAllCaseTypes(new CaseTypeFilterDTO());

        assertEquals(2, result.content().size());
        assertEquals(1, result.content().get(0).stages().size());
        assertEquals("Análisis", result.content().get(0).stages().get(0).name());
        assertTrue(result.content().get(1).stages().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllCaseTypes_emptyPage_returnsEmpty() {
        Page<CaseTypeModel> mockPage = new PageImpl<>(List.of());
        when(caseTypeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        PagedResponseDTO<CaseTypeDTO> result = caseTypeService.getAllCaseTypes(new CaseTypeFilterDTO());

        assertTrue(result.content().isEmpty());
        verify(stageRepository, never()).findByCaseTypeIdOrderByStageOrderAsc(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllCaseTypes_paginationParams_passedCorrectly() {
        Page<CaseTypeModel> mockPage = new PageImpl<>(List.of());
        org.mockito.ArgumentCaptor<Pageable> pageableCaptor =
                org.mockito.ArgumentCaptor.forClass(Pageable.class);
        when(caseTypeRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(mockPage);

        CaseTypeFilterDTO filter = new CaseTypeFilterDTO();
        filter.setPage(1);
        filter.setSize(5);
        filter.setSortBy("name");
        filter.setSortDir("asc");

        caseTypeService.getAllCaseTypes(filter);

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllCaseTypes_sizeExceedsMax_cappedAt100() {
        Page<CaseTypeModel> mockPage = new PageImpl<>(List.of());
        org.mockito.ArgumentCaptor<Pageable> pageableCaptor =
                org.mockito.ArgumentCaptor.forClass(Pageable.class);
        when(caseTypeRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(mockPage);

        CaseTypeFilterDTO filter = new CaseTypeFilterDTO();
        filter.setSize(500);

        caseTypeService.getAllCaseTypes(filter);

        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllCaseTypes_pageNegative_normalizedToZero() {
        Page<CaseTypeModel> mockPage = new PageImpl<>(List.of());
        org.mockito.ArgumentCaptor<Pageable> pageableCaptor =
                org.mockito.ArgumentCaptor.forClass(Pageable.class);
        when(caseTypeRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(mockPage);

        CaseTypeFilterDTO filter = new CaseTypeFilterDTO();
        filter.setPage(-3);

        caseTypeService.getAllCaseTypes(filter);

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllCaseTypes_returnsCorrectPageMetadata() {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        Page<CaseTypeModel> mockPage = new PageImpl<>(List.of(ct),
                org.springframework.data.domain.PageRequest.of(0, 10), 1L);
        when(caseTypeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        PagedResponseDTO<CaseTypeDTO> result = caseTypeService.getAllCaseTypes(new CaseTypeFilterDTO());

        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
    }

    // ── getCaseTypeById ───────────────────────────────────────────────────────

    @Test
    void getCaseTypeById_found_returnsDTOWithStages() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel s1 = buildStage(10L, ct, "Análisis", 1);
        CaseTypeStageModel s2 = buildStage(11L, ct, "Desarrollo", 2);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of(s1, s2));

        CaseTypeDTO result = caseTypeService.getCaseTypeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Bug", result.name());
        assertEquals(2, result.stages().size());
        assertEquals(1, result.stages().get(0).stageOrder());
        assertEquals(2, result.stages().get(1).stageOrder());
    }

    @Test
    void getCaseTypeById_notFound_throwsNotFoundException() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseTypeService.getCaseTypeById(99L));
        verify(stageRepository, never()).findByCaseTypeIdOrderByStageOrderAsc(any());
    }

    @Test
    void getCaseTypeById_noStages_returnsEmptyStagesList() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", null, true);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        CaseTypeDTO result = caseTypeService.getCaseTypeById(1L);

        assertTrue(result.stages().isEmpty());
    }

    // ── updateCaseType ────────────────────────────────────────────────────────

    @Test
    void updateCaseType_updatesNameAndDescription() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Old Name", "Old Desc", true);
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName("New Name");
        dto.setDescription("New Desc");

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(caseTypeRepository.save(ct)).thenReturn(ct);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        CaseTypeDTO result = caseTypeService.updateCaseType(1L, dto);

        assertEquals("New Name", result.name());
        assertEquals("New Desc", result.description());
        verify(caseTypeRepository).save(ct);
    }

    @Test
    void updateCaseType_emptyName_doesNotUpdateName() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Keep Name", "Old Desc", true);
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName("");
        dto.setDescription("New Desc");

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(caseTypeRepository.save(ct)).thenReturn(ct);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        caseTypeService.updateCaseType(1L, dto);

        assertEquals("Keep Name", ct.getName());
    }

    @Test
    void updateCaseType_nullDescription_doesNotClearDescription() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Name", "Keep Desc", true);
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName("New Name");
        dto.setDescription(null);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(caseTypeRepository.save(ct)).thenReturn(ct);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        caseTypeService.updateCaseType(1L, dto);

        assertEquals("Keep Desc", ct.getDescription());
    }

    @Test
    void updateCaseType_emptyStringDescription_clearsDescription() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Name", "Old Desc", true);
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName("Name");
        dto.setDescription("");

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(caseTypeRepository.save(ct)).thenReturn(ct);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        caseTypeService.updateCaseType(1L, dto);

        assertEquals("", ct.getDescription());
    }

    @Test
    void updateCaseType_notFound_throwsNotFoundException() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();

        assertThrows(NotFoundException.class, () -> caseTypeService.updateCaseType(99L, dto));
        verify(caseTypeRepository, never()).save(any());
    }

    @Test
    void updateCaseType_includesStagesInResponse() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel stage = buildStage(10L, ct, "QA", 3);
        UpdateCaseTypeDTO dto = new UpdateCaseTypeDTO();
        dto.setName("Bug actualizado");

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(caseTypeRepository.save(ct)).thenReturn(ct);
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of(stage));

        CaseTypeDTO result = caseTypeService.updateCaseType(1L, dto);

        assertEquals(1, result.stages().size());
        assertEquals("QA", result.stages().get(0).name());
    }

    // ── createStage ───────────────────────────────────────────────────────────

    @Test
    void createStage_savesAndReturnsStageDTO() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Desarrollo");
        dto.setDescription("Etapa de desarrollo");
        dto.setStageOrder(2);

        CaseTypeStageModel saved = buildStage(10L, ct, "Desarrollo", 2);
        saved.setDescription("Etapa de desarrollo");

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.existsByCaseTypeIdAndStageOrder(1L, 2)).thenReturn(false);
        when(stageRepository.save(any(CaseTypeStageModel.class))).thenReturn(saved);

        CaseTypeStageDTO result = caseTypeService.createStage(1L, dto);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("Desarrollo", result.name());
        assertEquals(2, result.stageOrder());
        assertTrue(result.active());
        verify(stageRepository).save(any(CaseTypeStageModel.class));
    }

    @Test
    void createStage_setsActiveTrueAndLinksToCaseType() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("QA");
        dto.setStageOrder(3);

        ArgumentCaptor<CaseTypeStageModel> captor = ArgumentCaptor.forClass(CaseTypeStageModel.class);
        CaseTypeStageModel saved = buildStage(20L, ct, "QA", 3);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.existsByCaseTypeIdAndStageOrder(1L, 3)).thenReturn(false);
        when(stageRepository.save(captor.capture())).thenReturn(saved);

        caseTypeService.createStage(1L, dto);

        assertTrue(captor.getValue().isActive());
        assertEquals(ct, captor.getValue().getCaseType());
        assertEquals(3, captor.getValue().getStageOrder());
    }

    @Test
    void createStage_caseTypeNotFound_throwsNotFoundException() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Stage");
        dto.setStageOrder(1);

        assertThrows(NotFoundException.class, () -> caseTypeService.createStage(99L, dto));
        verify(stageRepository, never()).save(any());
    }

    @Test
    void createStage_duplicateStageOrder_throwsDuplicateResourceException() {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CreateCaseTypeStageDTO dto = new CreateCaseTypeStageDTO();
        dto.setName("Análisis");
        dto.setStageOrder(1);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.existsByCaseTypeIdAndStageOrder(1L, 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> caseTypeService.createStage(1L, dto));
        verify(stageRepository, never()).save(any());
    }

    // ── getStages ─────────────────────────────────────────────────────────────

    @Test
    void getStages_returnsStagesOrderedByStageOrder() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel s1 = buildStage(10L, ct, "Análisis", 1);
        CaseTypeStageModel s2 = buildStage(11L, ct, "Desarrollo", 2);
        CaseTypeStageModel s3 = buildStage(12L, ct, "QA", 3);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of(s1, s2, s3));

        List<CaseTypeStageDTO> result = caseTypeService.getStages(1L);

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).stageOrder());
        assertEquals(2, result.get(1).stageOrder());
        assertEquals(3, result.get(2).stageOrder());
    }

    @Test
    void getStages_caseTypeNotFound_throwsNotFoundException() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseTypeService.getStages(99L));
        verify(stageRepository, never()).findByCaseTypeIdOrderByStageOrderAsc(any());
    }

    @Test
    void getStages_noStages_returnsEmptyList() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        List<CaseTypeStageDTO> result = caseTypeService.getStages(1L);

        assertTrue(result.isEmpty());
    }

    // ── updateStage ───────────────────────────────────────────────────────────

    @Test
    void updateStage_updatesNameDescriptionAndOrder() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel stage = buildStage(10L, ct, "Old Name", 1);
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setName("Desarrollo y pruebas");
        dto.setDescription("Nueva descripción");
        dto.setStageOrder(2);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(stage));
        when(stageRepository.existsByCaseTypeIdAndStageOrderAndIdNot(1L, 2, 10L)).thenReturn(false);
        when(stageRepository.save(stage)).thenReturn(stage);

        CaseTypeStageDTO result = caseTypeService.updateStage(1L, 10L, dto);

        assertEquals("Desarrollo y pruebas", result.name());
        assertEquals(2, result.stageOrder());
        verify(stageRepository).save(stage);
    }

    @Test
    void updateStage_emptyName_doesNotUpdateName() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel stage = buildStage(10L, ct, "Keep Name", 1);
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setName("");
        dto.setDescription("New Desc");

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(stage));
        when(stageRepository.save(stage)).thenReturn(stage);

        caseTypeService.updateStage(1L, 10L, dto);

        assertEquals("Keep Name", stage.getName());
    }

    @Test
    void updateStage_nullStageOrder_doesNotUpdateOrder() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel stage = buildStage(10L, ct, "Análisis", 1);
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setName("Análisis actualizado");
        dto.setStageOrder(null);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(stage));
        when(stageRepository.save(stage)).thenReturn(stage);

        caseTypeService.updateStage(1L, 10L, dto);

        assertEquals(1, stage.getStageOrder());
        verify(stageRepository, never()).existsByCaseTypeIdAndStageOrderAndIdNot(anyLong(), anyInt(), anyLong());
    }

    @Test
    void updateStage_duplicateStageOrder_throwsDuplicateResourceException() {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel stage = buildStage(10L, ct, "Análisis", 1);
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();
        dto.setStageOrder(2);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(stage));
        when(stageRepository.existsByCaseTypeIdAndStageOrderAndIdNot(1L, 2, 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> caseTypeService.updateStage(1L, 10L, dto));
        verify(stageRepository, never()).save(any());
    }

    @Test
    void updateStage_caseTypeNotFound_throwsNotFoundException() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();

        assertThrows(NotFoundException.class, () -> caseTypeService.updateStage(99L, 10L, dto));
        verify(stageRepository, never()).findByIdAndCaseTypeId(any(), any());
    }

    @Test
    void updateStage_stageNotFound_throwsNotFoundException() {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(99L, 1L)).thenReturn(Optional.empty());

        UpdateCaseTypeStageDTO dto = new UpdateCaseTypeStageDTO();

        assertThrows(NotFoundException.class, () -> caseTypeService.updateStage(1L, 99L, dto));
        verify(stageRepository, never()).save(any());
    }

    // ── deleteStage ───────────────────────────────────────────────────────────

    @Test
    void deleteStage_noActiveCases_deletesStage() throws NotFoundException {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel stage = buildStage(10L, ct, "Análisis", 1);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(stage));
        when(caseTypeRepository.existsActiveCaseTicketsByCaseTypeId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> caseTypeService.deleteStage(1L, 10L));

        verify(stageRepository).delete(stage);
    }

    @Test
    void deleteStage_withActiveCases_throwsBadRequestException() {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        CaseTypeStageModel stage = buildStage(10L, ct, "Análisis", 1);

        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.of(stage));
        when(caseTypeRepository.existsActiveCaseTicketsByCaseTypeId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> caseTypeService.deleteStage(1L, 10L));
        verify(stageRepository, never()).delete(any());
    }

    @Test
    void deleteStage_caseTypeNotFound_throwsNotFoundException() {
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseTypeService.deleteStage(99L, 10L));
        verify(stageRepository, never()).findByIdAndCaseTypeId(any(), any());
        verify(stageRepository, never()).delete(any());
    }

    @Test
    void deleteStage_stageNotFound_throwsNotFoundException() {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        when(stageRepository.findByIdAndCaseTypeId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseTypeService.deleteStage(1L, 99L));
        verify(stageRepository, never()).delete(any());
    }

    @Test
    void deleteStage_stageBelongsToDifferentCaseType_throwsNotFoundException() {
        CaseTypeModel ct = buildCaseType(1L, "Bug", "desc", true);
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(ct));
        // stageId=10 does not belong to caseTypeId=1
        when(stageRepository.findByIdAndCaseTypeId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseTypeService.deleteStage(1L, 10L));
        verify(caseTypeRepository, never()).existsActiveCaseTicketsByCaseTypeId(any());
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private static CaseTypeModel buildCaseType(Long id, String name, String description, boolean active) {
        CaseTypeModel ct = new CaseTypeModel();
        ct.setId(id);
        ct.setName(name);
        ct.setDescription(description);
        ct.setActive(active);
        ct.setCreatedAt(Instant.now());
        ct.setUpdatedAt(Instant.now());
        return ct;
    }

    private static CaseTypeStageModel buildStage(Long id, CaseTypeModel caseType,
                                                  String name, int stageOrder) {
        CaseTypeStageModel stage = new CaseTypeStageModel();
        stage.setId(id);
        stage.setCaseType(caseType);
        stage.setName(name);
        stage.setStageOrder(stageOrder);
        stage.setActive(true);
        stage.setCreatedAt(Instant.now());
        stage.setUpdatedAt(Instant.now());
        return stage;
    }
}
