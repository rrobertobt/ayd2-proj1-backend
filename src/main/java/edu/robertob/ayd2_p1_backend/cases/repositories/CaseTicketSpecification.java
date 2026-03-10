package edu.robertob.ayd2_p1_backend.cases.repositories;

import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CaseFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class CaseTicketSpecification {

    private CaseTicketSpecification() {}

    public static Specification<CaseTicketModel> from(CaseFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }

            if (filter.getCaseTypeId() != null) {
                predicates.add(cb.equal(root.get("caseType").get("id"), filter.getCaseTypeId()));
            }

            if (StringUtils.hasText(filter.getStatus())) {
                predicates.add(cb.equal(root.get("status"),
                        edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum.valueOf(
                                filter.getStatus().toUpperCase())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
