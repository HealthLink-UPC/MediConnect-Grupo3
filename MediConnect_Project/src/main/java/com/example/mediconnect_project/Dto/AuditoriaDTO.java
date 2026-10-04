package com.example.mediconnect_project.Dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditoriaDTO {

    private Long id_auditoria;
    private Long id_registro;
    private LocalDateTime fecha_creacion;
    private LocalDateTime fecha_edicion;
    private LocalDateTime fecha_eliminacion;
    private String tabla_afectada;
    private Integer estado_auditoria;
    private Long id_creacion;
    private Long id_edicion;
    private Long id_eliminacion;
}
