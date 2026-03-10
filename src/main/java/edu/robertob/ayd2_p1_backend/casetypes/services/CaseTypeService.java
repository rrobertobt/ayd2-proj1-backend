package edu.robertob.ayd2_p1_backend.casetypes.services;

import edu.robertob.ayd2_p1_backend.casetypes.models.dto.request.*;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.dto.response.CaseTypeStageDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeSpecification;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeStageRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.DuplicateResourceException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class CaseTypeService {

    private final CaseTypeRepository caseTypeRepository;
    private final CaseTypeStageRepository stageRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CASE TYPE - CREATE
    // ─────────────────────────────────────────────────────────────────────────

    public CaseTypeDTO createCaseType(CreateCaseTypeDTO dto) {
        CaseTypeModel caseType = new CaseTypeModel();
        caseType.setName(dto.getName());
        caseType.setDescription(dto.getDescription());
        caseType.setActive(true);
        CaseTypeModel saved = caseTypeRepository.save(caseType);
        return toDTO(saved, List.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASE TYPE - READ
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponseDTO<CaseTypeDTO> getAllCaseTypes(CaseTypeFilterDTO filter) {
        Pageable pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(Math.max(filter.getSize(), 1), 100)
        );

        Page<CaseTypeModel> page = caseTypeRepository.findAll(
                CaseTypeSpecification.from(filter), pageable);

        var content = page.getContent().stream()
                .map(ct -> {
                    List<CaseTypeStageModel> stages =
                            stageRepository.findByCaseTypeIdOrderByStageOrderAsc(ct.getId());
                    return toDTO(ct, stages);
                })
                .toList();

        return new PagedResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public CaseTypeDTO getCaseTypeById(Long caseTypeId) throws NotFoundException {
        CaseTypeModel caseType = findCaseTypeById(caseTypeId);
        List<CaseTypeStageModel> stages =
                stageRepository.findByCaseTypeIdOrderByStageOrderAsc(caseTypeId);
        return toDTO(caseType, stages);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASE TYPE - UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    public CaseTypeDTO updateCaseType(Long caseTypeId, UpdateCaseTypeDTO dto) throws NotFoundException {
        CaseTypeModel caseType = findCaseTypeById(caseTypeId);
        if (StringUtils.hasText(dto.getName())) {
            caseType.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            caseType.setDescription(dto.getDescription());
        }
        CaseTypeModel saved = caseTypeRepository.save(caseType);
        List<CaseTypeStageModel> stages =
                stageRepository.findByCaseTypeIdOrderByStageOrderAsc(caseTypeId);
        return toDTO(saved, stages);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE - CREATE
    // ─────────────────────────────────────────────────────────────────────────

    public CaseTypeStageDTO createStage(Long caseTypeId, CreateCaseTypeStageDTO dto) throws NotFoundException {
        CaseTypeModel caseType = findCaseTypeById(caseTypeId);

        if (stageRepository.existsByCaseTypeIdAndStageOrder(caseTypeId, dto.getStageOrder())) {
            throw new DuplicateResourceException(
                    "Ya existe una etapa con el orden " + dto.getStageOrder() +
                    " en el tipo de caso con ID: " + caseTypeId);
        }

        CaseTypeStageModel stage = new CaseTypeStageModel();
        stage.setCaseType(caseType);
        stage.setName(dto.getName());
        stage.setDescription(dto.getDescription());
        stage.setStageOrder(dto.getStageOrder());
        stage.setActive(true);

        CaseTypeStageModel saved = stageRepository.save(stage);
        return toStageDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE - READ
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CaseTypeStageDTO> getStages(Long caseTypeId) throws NotFoundException {
        findCaseTypeById(caseTypeId); // validate existence
        return stageRepository.findByCaseTypeIdOrderByStageOrderAsc(caseTypeId)
                .stream()
                .map(this::toStageDTO)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE - UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    public CaseTypeStageDTO updateStage(Long caseTypeId, Long stageId, UpdateCaseTypeStageDTO dto)
            throws NotFoundException {
        findCaseTypeById(caseTypeId); // validate existence
        CaseTypeStageModel stage = findStageByIdAndCaseTypeId(stageId, caseTypeId);

        if (dto.getStageOrder() != null &&
                stageRepository.existsByCaseTypeIdAndStageOrderAndIdNot(
                        caseTypeId, dto.getStageOrder(), stageId)) {
            throw new DuplicateResourceException(
                    "Ya existe una etapa con el orden " + dto.getStageOrder() +
                    " en el tipo de caso con ID: " + caseTypeId);
        }

        if (StringUtils.hasText(dto.getName())) {
            stage.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            stage.setDescription(dto.getDescription());
        }
        if (dto.getStageOrder() != null) {
            stage.setStageOrder(dto.getStageOrder());
        }

        CaseTypeStageModel saved = stageRepository.save(stage);
        return toStageDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE - DELETE
    // ─────────────────────────────────────────────────────────────────────────

    public void deleteStage(Long caseTypeId, Long stageId) throws NotFoundException {
        findCaseTypeById(caseTypeId); // validate existence
        CaseTypeStageModel stage = findStageByIdAndCaseTypeId(stageId, caseTypeId);

        // Restriction: cannot delete if case type has active cases
        // This check uses a native query via the repository to avoid pulling case_tickets module yet
        if (caseTypeRepository.existsActiveCaseTicketsByCaseTypeId(caseTypeId)) {
            throw new BadRequestException(
                    "No se puede eliminar la etapa porque el tipo de caso tiene casos activos asociados.");
        }

        stageRepository.delete(stage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private CaseTypeModel findCaseTypeById(Long id) throws NotFoundException {
        return caseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un tipo de caso con el ID: " + id));
    }

    private CaseTypeStageModel findStageByIdAndCaseTypeId(Long stageId, Long caseTypeId)
            throws NotFoundException {
        return stageRepository.findByIdAndCaseTypeId(stageId, caseTypeId)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró la etapa con ID " + stageId +
                        " en el tipo de caso con ID: " + caseTypeId));
    }

    private CaseTypeDTO toDTO(CaseTypeModel caseType, List<CaseTypeStageModel> stages) {
        return new CaseTypeDTO(
                caseType.getId(),
                caseType.getName(),
                caseType.getDescription(),
                caseType.isActive(),
                caseType.getCreatedAt(),
                caseType.getUpdatedAt(),
                stages.stream().map(this::toStageDTO).toList()
        );
    }

    private CaseTypeStageDTO toStageDTO(CaseTypeStageModel stage) {
        return new CaseTypeStageDTO(
                stage.getId(),
                stage.getName(),
                stage.getDescription(),
                stage.getStageOrder(),
                stage.isActive(),
                stage.getCreatedAt(),
                stage.getUpdatedAt()
        );
    }
}
