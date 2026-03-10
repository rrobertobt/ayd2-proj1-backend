package edu.robertob.ayd2_p1_backend.casetypes.models.dto.response;

import java.time.Instant;

public record CaseTypeStageDTO(
        Long id,
        String name,
        String description,
        int stageOrder,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
