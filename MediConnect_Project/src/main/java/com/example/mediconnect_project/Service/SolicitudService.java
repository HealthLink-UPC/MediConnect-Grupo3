package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.AprobacionDTO;
import com.example.mediconnect_project.Dto.RechazoDTO;
import com.example.mediconnect_project.Dto.RecetaDto;
import com.example.mediconnect_project.Dto.SolicitudNuevaRecetaDTO;
import com.example.mediconnect_project.Dto.SolicitudRenovacionDTO;
import com.example.mediconnect_project.Dto.SolicitudDto;

import java.util.List;

public interface SolicitudService {

    // Pide una receta nueva: el paciente elige el médico y los medicamentos
    SolicitudDto crear_nueva_receta(SolicitudNuevaRecetaDTO solicitud);

    // Renueva una receta existente: va al médico que la emitió y pide sus mismos medicamentos
    SolicitudDto crear_renovacion(SolicitudRenovacionDTO solicitud);

    List<SolicitudDto> listar_por_paciente(Long idPaciente);

    List<SolicitudDto> listar_pendientes_medico(Long idMedico);

    // Todas las solicitudes del médico, de la más reciente a la más antigua; el estado es opcional (1 a 4)
    List<SolicitudDto> listar_por_medico(Long idMedico, Integer estado);

    SolicitudDto marcar_en_revision(Long idSolicitud);

    RecetaDto aprobar(Long idSolicitud, AprobacionDTO aprobacion);

    SolicitudDto rechazar(Long idSolicitud, RechazoDTO rechazo);
}
