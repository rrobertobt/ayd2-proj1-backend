package edu.robertob.ayd2_p1_backend.casetypes.repositories;

import edu.robertob.ayd2_p1_backend.casetypes.models.dto.request.CaseTypeFilterDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CaseTypeSpecification {

    private CaseTypeSpecification() {}

    public static Specification<CaseTypeModel> from(CaseTypeFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getSearch())) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (filter.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
