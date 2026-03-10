package edu.robertob.ayd2_p1_backend.casetypes.models.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

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

    /** Campo de ordenamiento: name | createdAt */
    private String sortBy = "createdAt";

    /** Dirección: asc | desc */
    private String sortDir = "desc";

    public Sort.Direction direction() {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
