package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelCaseDTO {

    @NotBlank(message = "La razón de cancelación es requerida")
    private String reason;
}
