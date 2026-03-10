package edu.robertob.ayd2_p1_backend.casetypes.models.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseTypeFilterDTO {

    /** Búsqueda parcial por nombre (case-insensitive). */
    private String search;

    /** Filtrar por estado activo: true | false */
    private Boolean active;

    // ── Pagination ────────────────────────────────────────────────────────────

    private int page = 0;

    private int size = 10;
}
