package com.example.mediconnect_project.Dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DetalleRecetaDto {
    private Long id_tratamiento;
    private String nombre_medicamento;
    private String concentracion;
    private String dosis;
    private String frecuencia;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
}
