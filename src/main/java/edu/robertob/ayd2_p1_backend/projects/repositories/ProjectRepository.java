package edu.robertob.ayd2_p1_backend.projects.repositories;

import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectModel, Long>,
        JpaSpecificationExecutor<ProjectModel> {
}
