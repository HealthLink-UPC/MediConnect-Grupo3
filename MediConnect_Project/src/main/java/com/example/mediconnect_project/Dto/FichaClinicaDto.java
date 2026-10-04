package com.example.mediconnect_project.Dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FichaClinicaDto {
    private Long id_paciente;
    private String nombre_paciente;
    private String tipo_sangre;
    private Double peso;
    private Double talla;
    private List<String> alergias = new ArrayList<>();
    private List<String> enfermedades = new ArrayList<>();
    private String mensaje;
}
