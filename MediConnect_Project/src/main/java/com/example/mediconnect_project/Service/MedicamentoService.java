package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.MedicamentoDTO;

import java.util.List;

public interface MedicamentoService {

    List<MedicamentoDTO> listar();

    MedicamentoDTO guardar(MedicamentoDTO medicamento);

    // Baja lógica (estado 0); se audita la eliminación
    void eliminar(Long idMedicamento);
}
