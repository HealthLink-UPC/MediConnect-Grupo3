package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Segundo paso de la recuperación: el código recibido por correo y la nueva contraseña
@Data
public class RestablecerClaveDTO {

    @NotBlank(message = "Falta el identificador de la recuperación")
    private String id_reto;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 12, max = 50, message = "La nueva contraseña debe tener entre 12 y 50 caracteres")
    private String clave_nueva;
}
