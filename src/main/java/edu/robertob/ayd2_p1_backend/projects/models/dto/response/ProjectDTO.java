package edu.robertob.ayd2_p1_backend.projects.models.dto.response;

import java.time.Instant;

public record ProjectDTO(
        Long id,
        String name,
        String description,
        String status,
        Instant createdAt,
        Instant updatedAt,
        AdminInfoDTO currentAdmin
) {

    public record AdminInfoDTO(
            Long assignmentId,
            Long employeeId,
            String firstName,
            String lastName
    ) {}
}
