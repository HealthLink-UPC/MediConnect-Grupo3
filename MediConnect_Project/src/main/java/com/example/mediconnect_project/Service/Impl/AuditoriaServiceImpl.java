package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.AuditoriaDTO;
import com.example.mediconnect_project.Entity.AuditoriaEntity;
import com.example.mediconnect_project.Repository.AuditoriaRepository;
import com.example.mediconnect_project.Repository.UsuarioRepository;
import com.example.mediconnect_project.Service.AuditoriaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registra en tm_auditoria una fila por cada registro de las tablas del sistema:
 * quién lo creó (id_creacion) y cuándo, y luego quién lo editó o eliminó y cuándo.
 * La base permite una sola fila por (tabla_afectada, id_registro).
 */
@Slf4j
@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    public static final String TABLA_USUARIO = "tm_usuario";
    public static final String TABLA_MEDICO = "tm_medico";
    public static final String TABLA_PACIENTE = "tm_paciente";
    public static final String TABLA_SENSIBILIDAD = "tm_sensibilidad";
    public static final String TABLA_DIAGNOSTICO = "tm_diagnostico";
    public static final String TABLA_SOLICITUD = "tm_solicitud";
    public static final String TABLA_PEDIDO = "tm_pedido";
    public static final String TABLA_RECETA = "tm_receta";
    public static final String TABLA_TRATAMIENTO = "tm_tratamiento";
    public static final String TABLA_ALERGIA = "tm_alergia";
    public static final String TABLA_ENFERMEDAD = "tm_enfermedad";
    public static final String TABLA_MEDICAMENTO = "tm_medicamento";

    private static final int ESTADO_ACTIVO = 1;

    @Autowired
    private AuditoriaRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void registrar_alta(String tabla, Long idRegistro, Long idUsuario) {
        if (repository.buscar_registro(tabla, idRegistro).isPresent()) {
            return;
        }
        crear_fila(tabla, idRegistro, idUsuario);
    }

    @Override
    @Transactional
    public void registrar_edicion(String tabla, Long idRegistro, Long idUsuario) {
        // Si el registro es anterior a la auditoría, se crea la fila con quien lo modifica
        AuditoriaEntity auditoria = repository.buscar_registro(tabla, idRegistro)
                .orElseGet(() -> crear_fila(tabla, idRegistro, idUsuario));

        auditoria.setEdicion(usuarioRepository.getReferenceById(idUsuario));
        auditoria.setFecha_edicion(LocalDateTime.now());
        repository.save(auditoria);
    }

    @Override
    @Transactional
    public void registrar_eliminacion(String tabla, Long idRegistro, Long idUsuario) {
        AuditoriaEntity auditoria = repository.buscar_registro(tabla, idRegistro)
                .orElseGet(() -> crear_fila(tabla, idRegistro, idUsuario));

        auditoria.setEliminacion(usuarioRepository.getReferenceById(idUsuario));
        auditoria.setFecha_eliminacion(LocalDateTime.now());
        repository.save(auditoria);
    }

    @Override
    public List<AuditoriaDTO> listar() {
        List<AuditoriaEntity> listar = new ArrayList<>();
        listar = repository.listar_todo();

        List<AuditoriaDTO> listarDto = new ArrayList<>();

        for (AuditoriaEntity auditoriaEntity : listar) {

            AuditoriaDTO auditoriaDto = new AuditoriaDTO();

            auditoriaDto.setId_auditoria(auditoriaEntity.getId_auditoria());
            auditoriaDto.setId_registro(auditoriaEntity.getId_registro());
            auditoriaDto.setTabla_afectada(auditoriaEntity.getTabla_afectada());
            auditoriaDto.setFecha_creacion(auditoriaEntity.getFecha_creacion());
            auditoriaDto.setFecha_edicion(auditoriaEntity.getFecha_edicion());
            auditoriaDto.setFecha_eliminacion(auditoriaEntity.getFecha_eliminacion());
            auditoriaDto.setEstado_auditoria(auditoriaEntity.getEstado_auditoria());
            auditoriaDto.setId_creacion(auditoriaEntity.getCreacion().getId_usuario());
            if (auditoriaEntity.getEdicion() != null) {
                auditoriaDto.setId_edicion(auditoriaEntity.getEdicion().getId_usuario());
            }
            if (auditoriaEntity.getEliminacion() != null) {
                auditoriaDto.setId_eliminacion(auditoriaEntity.getEliminacion().getId_usuario());
            }

            listarDto.add(auditoriaDto);
        }

        return listarDto;
    }

    private AuditoriaEntity crear_fila(String tabla, Long idRegistro, Long idUsuario) {
        AuditoriaEntity auditoria = new AuditoriaEntity();
        auditoria.setCreacion(usuarioRepository.getReferenceById(idUsuario));
        auditoria.setId_registro(idRegistro);
        auditoria.setTabla_afectada(tabla);
        auditoria.setFecha_creacion(LocalDateTime.now());
        auditoria.setEstado_auditoria(ESTADO_ACTIVO);
        return repository.save(auditoria);
    }
}
