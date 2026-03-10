package edu.robertob.ayd2_p1_backend.reports.models.dto;

public record ProjectReportDTO(
        Long projectId,
        String projectName,
        String projectStatus,
        Long totalCases,
        Long openCases,
        Long inProgressCases,
        Long completedCases,
        Long canceledCases,
        Double totalHours,
        Double totalMoney
) {}
