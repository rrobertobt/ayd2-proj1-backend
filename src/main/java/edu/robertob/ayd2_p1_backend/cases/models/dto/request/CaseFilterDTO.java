package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class CaseFilterDTO {

    private Long projectId;
    private Long caseTypeId;
    private String status;

    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";

    public Sort.Direction direction() {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
