package edu.robertob.ayd2_p1_backend.cases.repositories;

import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseStepModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseStepRepository extends JpaRepository<CaseStepModel, Long> {

    List<CaseStepModel> findByCaseTicketIdOrderByStepOrderAsc(Long caseId);

    Optional<CaseStepModel> findByIdAndCaseTicketId(Long stepId, Long caseId);

    boolean existsByCaseTicketIdAndAssignedEmployeeId(Long caseTicketId, Long assignedEmployeeId);

    @Query("SELECT DISTINCT s.caseTicket.id FROM CaseStepModel s WHERE s.assignedEmployee.id = :employeeId")
    List<Long> findDistinctCaseIdsByAssignedEmployeeId(@Param("employeeId") Long employeeId);
}
