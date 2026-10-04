package com.example.mediconnect_project.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicamentoSolicitadoDto {
    private Long id_medicamento;
    private String nombre_medicamento;
    private String concentracion;
}
