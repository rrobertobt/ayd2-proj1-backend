package edu.robertob.ayd2_p1_backend.projects.repositories;

import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectMemberModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMemberModel, Long> {

    boolean existsByProjectIdAndEmployeeIdAndActiveTrue(Long projectId, Long employeeId);

    List<ProjectMemberModel> findByEmployeeIdAndActiveTrue(Long employeeId);
}
