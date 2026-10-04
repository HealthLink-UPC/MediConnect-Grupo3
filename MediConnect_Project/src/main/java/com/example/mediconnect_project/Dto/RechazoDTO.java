package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RechazoDTO {

    @NotBlank(message = "El motivo de rechazo es obligatorio")
    @Size(max = 500, message = "El motivo de rechazo no puede superar los 500 caracteres")
    private String motivo_rechazo;
}
