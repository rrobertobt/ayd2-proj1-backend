package edu.robertob.ayd2_p1_backend.projects.models.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectDTO {

    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String name;

    private String description;
}
