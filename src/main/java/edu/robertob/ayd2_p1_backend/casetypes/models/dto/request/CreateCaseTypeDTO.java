package edu.robertob.ayd2_p1_backend.casetypes.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCaseTypeDTO {

    @NotBlank(message = "El nombre del tipo de caso es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    private String name;

    private String description;
}
