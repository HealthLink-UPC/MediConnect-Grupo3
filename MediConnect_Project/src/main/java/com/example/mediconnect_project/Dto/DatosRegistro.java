package com.example.mediconnect_project.Dto;

import java.time.LocalDate;

// Datos que comparten el registro del paciente y el del médico (los DTO los implementan con sus getters)
public interface DatosRegistro {

    String getCorreo_usuario();

    String getClave_usuario();

    String getNombre_usuario();

    String getApellido_usuario();

    String getDni();

    LocalDate getFecha_nacimiento();

    String getTelefono();
}
