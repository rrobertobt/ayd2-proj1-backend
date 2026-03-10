package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import lombok.Data;

@Data
public class CaseFilterDTO {

    private Long projectId;
    private Long caseTypeId;
    private String status;

    private int page = 0;
    private int size = 10;
}
