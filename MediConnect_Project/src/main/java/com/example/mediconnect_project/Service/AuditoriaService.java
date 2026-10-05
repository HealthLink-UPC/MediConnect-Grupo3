package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.AuditoriaDTO;

import java.util.List;

public interface AuditoriaService {

    // idUsuario es siempre el id de tm_usuario de quien realiza la acción
    void registrar_alta(String tabla, Long idRegistro, Long idUsuario);

    void registrar_edicion(String tabla, Long idRegistro, Long idUsuario);

    void registrar_eliminacion(String tabla, Long idRegistro, Long idUsuario);

    List<AuditoriaDTO> listar();
}
