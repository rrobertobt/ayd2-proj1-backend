package edu.robertob.ayd2_p1_backend.casetypes.models.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCaseTypeDTO {

    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    private String name;

    private String description;
}
