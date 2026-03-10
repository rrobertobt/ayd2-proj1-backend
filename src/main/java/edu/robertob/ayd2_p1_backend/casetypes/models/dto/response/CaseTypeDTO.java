package edu.robertob.ayd2_p1_backend.casetypes.models.dto.response;

import java.time.Instant;
import java.util.List;

public record CaseTypeDTO(
        Long id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        List<CaseTypeStageDTO> stages
) {}
