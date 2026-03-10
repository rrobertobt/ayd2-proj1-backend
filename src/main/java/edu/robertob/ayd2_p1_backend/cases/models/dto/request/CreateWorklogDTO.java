package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWorklogDTO {

    @NotBlank(message = "El comentario es obligatorio")
    private String comment;

    @NotNull(message = "Las horas trabajadas son obligatorias")
    @Positive(message = "Las horas trabajadas deben ser mayores a 0")
    private Double hoursSpent;
}
