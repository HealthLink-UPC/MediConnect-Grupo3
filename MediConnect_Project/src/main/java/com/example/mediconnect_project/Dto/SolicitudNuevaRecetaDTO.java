package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// Solicitud de una receta nueva: el paciente elige el médico y los medicamentos.
// El paciente que solicita no se envía: se toma del token de quien hace la petición
@Data
public class SolicitudNuevaRecetaDTO {

    @NotNull(message = "El médico es obligatorio")
    private Long id_medico;

    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;

    @NotEmpty(message = "Debe seleccionar al menos un medicamento")
    private List<Long> id_medicamentos = new ArrayList<>();
}
