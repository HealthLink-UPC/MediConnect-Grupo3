package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Solicitud de renovación: se indica la receta a renovar. Va al médico que la emitió y pide sus mismos medicamentos.
// El paciente que solicita no se envía: se toma del token de quien hace la petición
@Data
public class SolicitudRenovacionDTO {

    @NotNull(message = "La receta a renovar es obligatoria")
    private Long id_receta;

    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;
}
