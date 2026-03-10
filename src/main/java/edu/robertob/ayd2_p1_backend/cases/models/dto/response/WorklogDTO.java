package edu.robertob.ayd2_p1_backend.cases.models.dto.response;

import java.time.Instant;

public record WorklogDTO(
        Long id,
        Long caseStepId,
        Long employeeId,
        String employeeName,
        String comment,
        Double hoursSpent,
        Instant createdAt,
        Instant updatedAt
) {}
