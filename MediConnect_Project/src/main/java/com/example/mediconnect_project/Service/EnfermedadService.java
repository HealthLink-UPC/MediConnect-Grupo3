package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.EnfermedadDTO;

import java.util.List;

public interface EnfermedadService {

    List<EnfermedadDTO> listar();

    EnfermedadDTO guardar(EnfermedadDTO enfermedad);

    // Baja lógica (estado 0); se audita la eliminación
    void eliminar(Long idEnfermedad);
}
