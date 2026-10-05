package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.AlergiaDTO;

import java.util.List;

public interface AlergiaService {

    List<AlergiaDTO> listar();

    AlergiaDTO guardar(AlergiaDTO alergia);

    // Baja lógica (estado 0); se audita la eliminación
    void eliminar(Long idAlergia);
}
