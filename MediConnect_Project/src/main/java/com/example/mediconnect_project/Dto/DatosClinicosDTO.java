package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DatosClinicosDTO {

    @NotBlank(message = "El tipo de sangre es obligatorio")
    @Size(max = 50, message = "El tipo de sangre no puede superar los 50 caracteres")
    private String tipo_sangre;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor a cero")
    private Double peso;

    @NotNull(message = "La talla es obligatoria")
    @Positive(message = "La talla debe ser mayor a cero")
    private Double talla;
}
