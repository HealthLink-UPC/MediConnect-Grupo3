package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Primer paso de la recuperación: el correo de la cuenta
@Data
public class RecuperarClaveDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo_usuario;
}
