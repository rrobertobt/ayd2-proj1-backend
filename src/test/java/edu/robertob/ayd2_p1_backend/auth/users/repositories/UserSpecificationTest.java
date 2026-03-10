package edu.robertob.ayd2_p1_backend.auth.users.repositories;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UserFilterDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserSpecificationTest {

    @SuppressWarnings("unchecked")
    private final Root<UserModel> root = mock(Root.class);
    @SuppressWarnings("unchecked")
    private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    @SuppressWarnings("unchecked")
    private final Join<UserModel, EmployeeModel> empJoin = mock(Join.class);
    @SuppressWarnings("unchecked")
    private final Path<Object> pathObj = mock(Path.class);
    private final Predicate predicate = mock(Predicate.class);
    private final Predicate andPredicate = mock(Predicate.class);

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(root.join(anyString(), any(JoinType.class))).thenReturn((Join) empJoin);
        when(query.getResultType()).thenReturn((Class) UserModel.class);
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.lower(any())).thenReturn(mock(Expression.class));
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);
        when(root.get(anyString())).thenReturn(pathObj);
        when(empJoin.get(anyString())).thenReturn(pathObj);
        when(pathObj.get(anyString())).thenReturn(pathObj);
    }

    @Test
    void from_emptyFilter_returnsAndPredicate() {
        UserFilterDTO filter = new UserFilterDTO();

        Specification<UserModel> spec = UserSpecification.from(filter);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(andPredicate, result);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void from_withSearch_addsOrPredicate() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setSearch("alice");

        Specification<UserModel> spec = UserSpecification.from(filter);
        spec.toPredicate(root, query, cb);

        verify(cb).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    void from_withFirstName_addsLikePredicateOnEmpJoin() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setFirstName("John");

        Specification<UserModel> spec = UserSpecification.from(filter);
        spec.toPredicate(root, query, cb);

        verify(empJoin).get("first_name");
    }

    @Test
    void from_withLastName_addsLikePredicateOnEmpJoin() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setLastName("Doe");

        Specification<UserModel> spec = UserSpecification.from(filter);
        spec.toPredicate(root, query, cb);

        verify(empJoin).get("last_name");
    }

    @Test
    void from_withEmail_addsEqualPredicate() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setEmail("test@mail.com");

        Specification<UserModel> spec = UserSpecification.from(filter);
        spec.toPredicate(root, query, cb);

        // Verify that root.get("email") was accessed — key step for the email equal-predicate path
        verify(root, atLeastOnce()).get("email");
    }

    @Test
    void from_withRoleId_addsRolePredicate() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setRoleId(1L);

        Specification<UserModel> spec = UserSpecification.from(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("role");
    }

    @Test
    void from_withActive_addsActivePredicate() {
        UserFilterDTO filter = new UserFilterDTO();
        filter.setActive(true);

        Specification<UserModel> spec = UserSpecification.from(filter);
        spec.toPredicate(root, query, cb);

        verify(root, atLeastOnce()).get("active");
    }

    @Test
    @SuppressWarnings("unchecked")
    void from_withLongResultType_skipsDistinct() {
        when(query.getResultType()).thenReturn((Class) Long.class);
        UserFilterDTO filter = new UserFilterDTO();

        Specification<UserModel> spec = UserSpecification.from(filter);
        spec.toPredicate(root, query, cb);

        verify(query, never()).distinct(anyBoolean());
    }
}
