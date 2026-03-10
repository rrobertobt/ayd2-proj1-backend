package edu.robertob.ayd2_p1_backend.cases.models.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CaseDTO(
        Long id,
        Long projectId,
        String projectName,
        Long caseTypeId,
        String caseTypeName,
        Long createdByEmployeeId,
        String createdByEmployeeName,
        String title,
        String description,
        String status,
        LocalDate dueDate,
        boolean overdue,
        int progressPercent,
        Instant canceledAt,
        String cancelReason,
        Instant createdAt,
        Instant updatedAt,
        List<CaseStepDTO> steps
) {}
