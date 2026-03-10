package edu.robertob.ayd2_p1_backend.reports.models.dto;

public record ProjectCaseCountDTO(
        Long projectId,
        String projectName,
        String projectStatus,
        Long caseCount
) {}
