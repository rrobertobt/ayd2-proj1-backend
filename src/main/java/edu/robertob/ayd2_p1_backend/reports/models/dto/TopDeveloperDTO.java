package edu.robertob.ayd2_p1_backend.reports.models.dto;

public record TopDeveloperDTO(
        Long employeeId,
        String firstName,
        String lastName,
        String username,
        Long count,
        Double totalMoney
) {}
