package edu.robertob.ayd2_p1_backend.cases.models.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateDueDateDTO {

    @NotNull(message = "La fecha límite es requerida")
    @Future(message = "La fecha límite debe ser una fecha futura")
    private LocalDate dueDate;
}
