package edu.robertob.ayd2_p1_backend.projects.repositories;

import edu.robertob.ayd2_p1_backend.projects.models.dto.request.ProjectFilterDTO;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectSpecificationTest {

    @SuppressWarnings("unchecked")
    private final Root<ProjectModel> root = mock(Root.class);
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
        Specification<ProjectModel> spec = ProjectSpecification.from(new ProjectFilterDTO());
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(andPredicate, result);
        verify(cb, never()).like(any(), anyString());
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void from_withSearch_addsLikePredicate() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setSearch("alpha");

        ProjectSpecification.from(filter).toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%alpha%"));
        verify(root).get("name");
    }

    @Test
    void from_withValidStatus_addsEqualPredicate() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setStatus("active");

        ProjectSpecification.from(filter).toPredicate(root, query, cb);

        verify(cb).equal(path, edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum.ACTIVE);
    }

    @Test
    void from_withInvalidStatus_addsNoPredicate() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setStatus("UNKNOWN");

        ProjectSpecification.from(filter).toPredicate(root, query, cb);

        verify(cb, never()).equal(any(), any());
    }

    @Test
    void from_withSearchAndStatus_addsBothPredicates() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setSearch("beta");
        filter.setStatus("INACTIVE");

        ProjectSpecification.from(filter).toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%beta%"));
        verify(cb).equal(path, edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum.INACTIVE);
    }
}
