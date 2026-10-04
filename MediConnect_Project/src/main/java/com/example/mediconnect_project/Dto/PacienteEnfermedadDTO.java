package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PacienteEnfermedadDTO {

    @NotNull(message = "La enfermedad es obligatoria")
    private Long id_enfermedad;
}
