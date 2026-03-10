package edu.robertob.ayd2_p1_backend.reports.models.dto;

import java.time.Instant;
import java.time.LocalDate;

public record CaseReportDTO(
        Long caseId,
        String title,
        String status,
        String projectName,
        String caseTypeName,
        String createdByEmployee,
        LocalDate dueDate,
        Instant createdAt,
        Double totalHours,
        Double totalMoney
) {}
