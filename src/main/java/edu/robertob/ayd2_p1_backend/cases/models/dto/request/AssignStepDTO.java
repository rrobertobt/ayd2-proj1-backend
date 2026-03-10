package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignStepDTO {

    @NotNull(message = "El userId del desarrollador es obligatorio")
    private Long userId;
}
