package edu.robertob.ayd2_p1_backend.projects.models.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignProjectAdminDTO {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;
}
