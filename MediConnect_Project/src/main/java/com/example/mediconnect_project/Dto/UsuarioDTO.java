package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    @NotBlank(message = "El correo es obligatorio")
    private String correo_usuario;
    @NotBlank(message = "La contraseña es obligatoria")
    private String clave_usuario;
}
