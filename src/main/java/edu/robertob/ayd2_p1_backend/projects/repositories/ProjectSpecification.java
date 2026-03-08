package edu.robertob.ayd2_p1_backend.projects.repositories;

import edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.ProjectFilterDTO;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecification {

    private ProjectSpecification() {}

    public static Specification<ProjectModel> from(ProjectFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getSearch())) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (StringUtils.hasText(filter.getStatus())) {
                try {
                    ProjectStatusEnum statusEnum =
                            ProjectStatusEnum.valueOf(filter.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    // invalid status → no predicate added; service validates separately if needed
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
