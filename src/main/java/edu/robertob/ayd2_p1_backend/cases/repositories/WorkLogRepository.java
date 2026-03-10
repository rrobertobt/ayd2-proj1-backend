package edu.robertob.ayd2_p1_backend.cases.repositories;

import edu.robertob.ayd2_p1_backend.cases.models.entities.WorkLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLogModel, Long> {

    List<WorkLogModel> findByCaseStepIdOrderByCreatedAtAsc(Long caseStepId);
}
