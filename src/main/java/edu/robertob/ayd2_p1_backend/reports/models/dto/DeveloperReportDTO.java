package edu.robertob.ayd2_p1_backend.reports.models.dto;

public record DeveloperReportDTO(
        Long employeeId,
        String firstName,
        String lastName,
        String username,
        Double hourlyRate,
        Long totalCasesParticipated,
        Double totalHoursLogged,
        Double totalMoneyPaid
) {}
