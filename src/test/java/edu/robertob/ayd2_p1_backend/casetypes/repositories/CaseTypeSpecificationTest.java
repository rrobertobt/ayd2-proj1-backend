package edu.robertob.ayd2_p1_backend.casetypes.repositories;

import edu.robertob.ayd2_p1_backend.casetypes.models.dto.request.CaseTypeFilterDTO;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CaseTypeSpecificationTest {

    @SuppressWarnings("unchecked")
    private final Root<CaseTypeModel> root = mock(Root.class);
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
        when(cb.lower(any())).thenReturn(mock(Expression.class));
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);
    }

    @Test
    void from_emptyFilter_returnsAndWithNoPredicates() {
        Specification<CaseTypeModel> spec = CaseTypeSpecification.from(new CaseTypeFilterDTO());
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(andPredicate, result);
        verify(cb).and(any(Predicate[].class));
        verify(cb, never()).like(any(), anyString());
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void from_withSearch_addsLikePredicate() {
        CaseTypeFilterDTO filter = new CaseTypeFilterDTO();
        filter.setSearch("bug");

        CaseTypeSpecification.from(filter).toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%bug%"));
        verify(root).get("name");
    }

    @Test
    void from_withActiveTrue_addsEqualPredicate() {
        CaseTypeFilterDTO filter = new CaseTypeFilterDTO();
        filter.setActive(true);

        CaseTypeSpecification.from(filter).toPredicate(root, query, cb);

        verify(cb).equal(path, true);
    }

    @Test
    void from_withSearchAndActive_addsBothPredicates() {
        CaseTypeFilterDTO filter = new CaseTypeFilterDTO();
        filter.setSearch("task");
        filter.setActive(false);

        CaseTypeSpecification.from(filter).toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%task%"));
        verify(cb).equal(path, false);
    }
}
