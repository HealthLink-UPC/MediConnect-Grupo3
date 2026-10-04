package com.example.mediconnect_project.Dto;


import lombok.Data;

@Data
public class MedicamentoDTO {

    private Long id_medicamento;
    private String nombre_medicamento;
    private String concentracion;
    private Integer estado_medicamento;
}
