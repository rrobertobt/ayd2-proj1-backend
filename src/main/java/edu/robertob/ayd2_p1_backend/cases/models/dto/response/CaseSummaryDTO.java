package edu.robertob.ayd2_p1_backend.cases.models.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record CaseSummaryDTO(
        Long id,
        Long projectId,
        String projectName,
        Long caseTypeId,
        String caseTypeName,
        String title,
        String status,
        LocalDate dueDate,
        boolean overdue,
        int progressPercent,
        Instant createdAt,
        Instant updatedAt
) {}
