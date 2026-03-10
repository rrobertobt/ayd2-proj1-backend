package edu.robertob.ayd2_p1_backend.projects.models.dto.request;

import lombok.Getter;
import lombok.Setter;

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
}
