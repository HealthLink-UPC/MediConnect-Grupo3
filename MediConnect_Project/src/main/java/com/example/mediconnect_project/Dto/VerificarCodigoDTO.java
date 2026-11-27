package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Segundo paso del inicio de sesión: el código que llegó al correo
@Data
public class VerificarCodigoDTO {

    @NotBlank(message = "El identificador del inicio de sesión es obligatorio")
    private String id_reto;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}
