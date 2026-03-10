package edu.robertob.ayd2_p1_backend.cases.models.dto.response;

import java.time.Instant;

public record CaseStepDTO(
        Long id,
        Long caseTypeStageId,
        String stageName,
        int stepOrder,
        String status,
        Long assignedEmployeeId,
        String assignedEmployeeName,
        Instant assignedAt,
        Instant startedAt,
        Instant submittedAt,
        Instant approvedAt,
        Instant rejectedAt,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {}
