package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PacienteAlergiaDTO {

    @NotNull(message = "La alergia es obligatoria")
    private Long id_alergia;
}
