package edu.robertob.ayd2_p1_backend.cases.repositories;

import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CaseFilterDTO;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CaseTicketSpecificationTest {

    @SuppressWarnings("unchecked")
    private final Root<CaseTicketModel> root = mock(Root.class);
    @SuppressWarnings("unchecked")
    private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    @SuppressWarnings("unchecked")
    private final Path<Object> path = mock(Path.class);
    private final Predicate predicate = mock(Predicate.class);
    private final Predicate andPredicate = mock(Predicate.class);

    @BeforeEach
    void setUp() {
        when(root.get(anyString())).thenReturn(path);
        when(path.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);
    }

    @Test
    void from_emptyFilter_noPredicates() {
        Specification<CaseTicketModel> spec = CaseTicketSpecification.from(new CaseFilterDTO());
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(andPredicate, result);
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void from_withProjectId_addsProjectPredicate() {
        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setProjectId(1L);

        CaseTicketSpecification.from(filter).toPredicate(root, query, cb);

        verify(root).get("project");
        verify(cb).equal(path, 1L);
    }

    @Test
    void from_withCaseTypeId_addsCaseTypePredicate() {
        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setCaseTypeId(2L);

        CaseTicketSpecification.from(filter).toPredicate(root, query, cb);

        verify(root).get("caseType");
        verify(cb).equal(path, 2L);
    }

    @Test
    void from_withStatus_addsStatusPredicate() {
        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setStatus("OPEN");

        CaseTicketSpecification.from(filter).toPredicate(root, query, cb);

        verify(root).get("status");
        verify(cb).equal(path, CaseStatusEnum.OPEN);
    }

    @Test
    void from_allFilters_addsAllPredicates() {
        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setProjectId(1L);
        filter.setCaseTypeId(2L);
        filter.setStatus("IN_PROGRESS");

        CaseTicketSpecification.from(filter).toPredicate(root, query, cb);

        verify(root).get("project");
        verify(root).get("caseType");
        verify(root).get("status");
        verify(cb).equal(path, CaseStatusEnum.IN_PROGRESS);
    }
}
