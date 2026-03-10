package edu.robertob.ayd2_p1_backend.projects.repositories;

import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectAdminAssignmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAdminAssignmentRepository extends JpaRepository<ProjectAdminAssignmentModel, Long> {

    Optional<ProjectAdminAssignmentModel> findByProjectIdAndActiveTrue(Long projectId);

    List<ProjectAdminAssignmentModel> findByEmployeeIdAndActiveTrue(Long employeeId);
}
