package com.example.mediconnect_project.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRespuestaDTO {
    private String token;
    private Long id_usuario;
    private String rol_usuario;
    private String nombre_usuario;
    private Long id_paciente;
    private Long id_medico;

    // Verificación en dos pasos: si requiere_codigo es true no hay token todavía; falta enviar el código recibido por correo
    private Boolean requiere_codigo;
    private String id_reto;
    private String correo_enmascarado;
}
