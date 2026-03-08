package edu.robertob.ayd2_p1_backend.casetypes.repositories;

import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseTypeStageRepository extends JpaRepository<CaseTypeStageModel, Long> {

    List<CaseTypeStageModel> findByCaseTypeIdOrderByStageOrderAsc(Long caseTypeId);

    boolean existsByCaseTypeIdAndStageOrder(Long caseTypeId, int stageOrder);

    boolean existsByCaseTypeIdAndStageOrderAndIdNot(Long caseTypeId, int stageOrder, Long excludeId);

    Optional<CaseTypeStageModel> findByIdAndCaseTypeId(Long id, Long caseTypeId);
}
