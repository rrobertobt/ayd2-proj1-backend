package edu.robertob.ayd2_p1_backend.casetypes.models.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCaseTypeStageDTO {

    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    private String name;

    private String description;

    @Min(value = 1, message = "El orden de la etapa debe ser al menos 1.")
    private Integer stageOrder;
}
