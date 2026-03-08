package edu.robertob.ayd2_p1_backend.casetypes.repositories;

import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseTypeRepository extends JpaRepository<CaseTypeModel, Long> {

    List<CaseTypeModel> findAllByOrderByCreatedAtDesc();

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM case_tickets
                WHERE case_type_id = :caseTypeId
                AND status NOT IN ('COMPLETED', 'CANCELED')
            )
            """, nativeQuery = true)
    boolean existsActiveCaseTicketsByCaseTypeId(@Param("caseTypeId") Long caseTypeId);
}
