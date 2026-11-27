package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Cambio de contraseña: el usuario confirma su clave actual y define una nueva
@Data
public class CambioClaveDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String clave_actual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 12, max = 50, message = "La nueva contraseña debe tener entre 12 y 50 caracteres")
    private String clave_nueva;
}
