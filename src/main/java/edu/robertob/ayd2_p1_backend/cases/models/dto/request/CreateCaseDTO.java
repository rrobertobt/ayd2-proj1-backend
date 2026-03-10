package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCaseDTO {

    @NotNull(message = "El ID del proyecto es requerido")
    private Long projectId;

    @NotNull(message = "El ID del tipo de caso es requerido")
    private Long caseTypeId;

    @NotBlank(message = "El título es requerido")
    @Size(max = 250, message = "El título no puede superar 250 caracteres")
    private String title;

    private String description;

    @NotNull(message = "La fecha límite es requerida")
    @Future(message = "La fecha límite debe ser una fecha futura")
    private LocalDate dueDate;
}
