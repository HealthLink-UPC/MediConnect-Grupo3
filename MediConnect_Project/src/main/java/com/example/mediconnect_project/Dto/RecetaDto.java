package com.example.mediconnect_project.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecetaDto {
    private Long id_receta;
    private LocalDate fecha_emision;
    private LocalDate fecha_vencimiento;
    private String codigo_verificacion;
    private Integer estado_receta;
    private String estado_nombre;
    private Long id_paciente;
    private String nombre_paciente;
    private Long id_medico;
    private String nombre_medico;
    private List<DetalleRecetaDto> detalles = new ArrayList<>();
}
