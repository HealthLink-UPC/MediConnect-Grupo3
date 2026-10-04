package com.example.mediconnect_project.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

// Para editar el perfil solo se usan correo_usuario y telefono; el resto es de solo lectura
@Data
public class PerfilDto {
    private Long id_usuario;
    private String rol_usuario;
    private String nombre_usuario;
    private String apellido_usuario;
    private String dni;
    private LocalDate fecha_nacimiento;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String correo_usuario;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    private String telefono;
}
