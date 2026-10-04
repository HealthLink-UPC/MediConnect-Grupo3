package com.example.mediconnect_project.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class SolicitudDto {
    private Long id_solicitud;
    private LocalDate fecha_solicitud;
    private String tipo_solicitud;
    private String motivo;
    private String motivo_estado;
    private Integer estado_solicitud;
    private String estado_nombre;
    private Long id_paciente;
    private String nombre_paciente;
    private Long id_medico;
    private String nombre_medico;
    private Long id_receta;
    private List<MedicamentoSolicitadoDto> medicamentos = new ArrayList<>();
}
