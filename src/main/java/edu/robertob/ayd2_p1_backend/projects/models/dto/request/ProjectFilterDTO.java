package edu.robertob.ayd2_p1_backend.projects.models.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

/**
 * Query-parameter filter bag for the project listing endpoint.
 * All fields are optional; only non-null/non-blank values are applied as predicates.
 */
@Getter
@Setter
public class ProjectFilterDTO {

    /** Free-text search on project name (case-insensitive, partial match). */
    private String search;

    /** Filter by project status: ACTIVE | INACTIVE */
    private String status;

    // ── Pagination ────────────────────────────────────────────────────────────

    private int page = 0;

    private int size = 10;

    /** Field to sort by: name | status | createdAt */
    private String sortBy = "createdAt";

    /** Sort direction: asc | desc */
    private String sortDir = "desc";

    public Sort.Direction direction() {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
