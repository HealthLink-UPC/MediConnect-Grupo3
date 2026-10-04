package com.example.mediconnect_project.Dto;

import lombok.Data;

@Data
public class MedicoDto {
    private Long id_medico;
    private String nombre_medico;
    private String especialidad;
    private String cmp;
    private String centro_atencion;
}
