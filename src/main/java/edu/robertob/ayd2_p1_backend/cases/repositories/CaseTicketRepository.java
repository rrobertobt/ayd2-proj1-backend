package edu.robertob.ayd2_p1_backend.cases.repositories;

import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseTicketRepository extends JpaRepository<CaseTicketModel, Long>,
        JpaSpecificationExecutor<CaseTicketModel> {

    List<CaseTicketModel> findByProjectId(Long projectId);

    List<CaseTicketModel> findByProjectIdIn(List<Long> projectIds);
}
