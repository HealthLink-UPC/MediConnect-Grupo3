package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ItemRecetaDTO {

    @NotNull(message = "El medicamento es obligatorio")
    private Long id_medicamento;

    @NotBlank(message = "La dosis es obligatoria")
    @Size(max = 100, message = "La dosis no puede superar los 100 caracteres")
    private String dosis;

    @NotBlank(message = "La frecuencia es obligatoria")
    @Size(max = 50, message = "La frecuencia no puede superar los 50 caracteres")
    private String frecuencia;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fecha_inicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fecha_fin;
}
