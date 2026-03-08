package edu.robertob.ayd2_p1_backend.auth.users.repositories;

import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UserFilterDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for dynamic user filtering.
 * Performs a LEFT JOIN to the employees table so users without an employee
 * profile are still returned unless an employee-specific filter is active.
 */
public final class UserSpecification {

    private UserSpecification() {}

    public static Specification<UserModel> from(UserFilterDTO filter) {
        return (root, query, cb) -> {

            // Left-join employees so users without profiles are not excluded
            Join<UserModel, EmployeeModel> empJoin =
                    root.join("employee", JoinType.LEFT);

            // Avoid duplicate rows when using join + count queries
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            // ── General search: username OR email (partial, case-insensitive) ──
            if (StringUtils.hasText(filter.getSearch())) {
                String pattern = likePattern(filter.getSearch());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }

            // ── Employee first name (partial, case-insensitive) ──────────────
            if (StringUtils.hasText(filter.getFirstName())) {
                predicates.add(cb.like(
                        cb.lower(empJoin.get("first_name")),
                        likePattern(filter.getFirstName())
                ));
            }

            // ── Employee last name (partial, case-insensitive) ───────────────
            if (StringUtils.hasText(filter.getLastName())) {
                predicates.add(cb.like(
                        cb.lower(empJoin.get("last_name")),
                        likePattern(filter.getLastName())
                ));
            }

            // ── Employee full name (partial, case-insensitive) ───────────────
            // Matches against "firstName lastName" and "lastName firstName" so
            // the caller can supply names in any order.
            if (StringUtils.hasText(filter.getFullName())) {
                String pattern = likePattern(filter.getFullName());
                Expression<String> firstLast = cb.lower(cb.concat(
                        cb.concat(empJoin.get("first_name"), " "),
                        empJoin.get("last_name")
                ));
                Expression<String> lastFirst = cb.lower(cb.concat(
                        cb.concat(empJoin.get("last_name"), " "),
                        empJoin.get("first_name")
                ));
                predicates.add(cb.or(
                        cb.like(firstLast, pattern),
                        cb.like(lastFirst, pattern)
                ));
            }

            // ── Exact email ──────────────────────────────────────────────────
            if (StringUtils.hasText(filter.getEmail())) {
                predicates.add(cb.equal(
                        cb.lower(root.get("email")),
                        filter.getEmail().toLowerCase()
                ));
            }

            // ── Role ID ──────────────────────────────────────────────────────
            if (filter.getRoleId() != null) {
                predicates.add(cb.equal(root.get("role").get("id"), filter.getRoleId()));
            }

            // ── Role Code ────────────────────────────────────────────────────
            if (filter.getRoleCode() != null) {
                predicates.add(cb.equal(root.get("role").get("code"), filter.getRoleCode()));
            }

            // ── Active status ────────────────────────────────────────────────
            if (filter.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String likePattern(String value) {
        return "%" + value.toLowerCase().trim() + "%";
    }
}

