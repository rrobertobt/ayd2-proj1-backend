package edu.robertob.ayd2_p1_backend.auth.users.models.dto.request;

import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

/**
 * Query-parameter filter bag for the user listing endpoint.
 * All fields are optional; only non-null values are applied as predicates.
 */
@Getter
@Setter
public class UserFilterDTO {

    /** Free-text search across username and email (case-insensitive, partial match). */
    private String search;

    /** Filter by employee first name (case-insensitive, partial match). */
    private String firstName;

    /** Filter by employee last name (case-insensitive, partial match). */
    private String lastName;

    /** Filter by employee full name (case-insensitive, partial match on "firstName lastName" or "lastName firstName"). */
    private String fullName;

    /** Filter by exact user email. */
    private String email;

    /** Filter by role ID. */
    private Long roleId;

    /** Filter by role code enum (e.g. SYSTEM_ADMIN, PROJECT_ADMIN, DEVELOPER). */
    private RolesEnum roleCode;

    /** Filter by active/inactive status. */
    private Boolean active;

    // ── Pagination ────────────────────────────────────────────────────────────

    private int page = 0;

    private int size = 10;

    /** Field to sort by: username | email | active | createdAt | firstName | lastName | hourlyRate */
    private String sortBy = "createdAt";

    /** Sort direction: asc | desc */
    private String sortDir = "desc";

    public Sort.Direction direction() {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}

