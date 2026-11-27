package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Pide un código nuevo para un inicio de sesión que está esperando verificación
@Data
public class ReenviarCodigoDTO {

    @NotBlank(message = "El identificador del inicio de sesión es obligatorio")
    private String id_reto;
}
