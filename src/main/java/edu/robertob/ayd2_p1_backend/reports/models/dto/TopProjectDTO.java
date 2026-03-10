package edu.robertob.ayd2_p1_backend.reports.models.dto;

public record TopProjectDTO(
        Long projectId,
        String projectName,
        Long caseCount
) {}
