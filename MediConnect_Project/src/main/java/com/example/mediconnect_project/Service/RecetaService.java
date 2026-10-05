package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.ItemRecetaDTO;
import com.example.mediconnect_project.Dto.RecetaDto;
import com.example.mediconnect_project.Entity.SolicitudEntity;

import java.util.List;

public interface RecetaService {

    RecetaDto emitir(SolicitudEntity solicitud, List<ItemRecetaDTO> items);

    List<RecetaDto> listar_historial(Long idPaciente);

    RecetaDto obtener_detalle(Long idReceta);
}
