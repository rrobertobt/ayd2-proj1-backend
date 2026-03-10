package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectStepDTO {

    @NotBlank(message = "La razón de rechazo es obligatoria")
    private String reason;
}
