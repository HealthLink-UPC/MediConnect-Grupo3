package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.AprobacionDTO;
import com.example.mediconnect_project.Dto.MedicamentoSolicitadoDto;
import com.example.mediconnect_project.Dto.RechazoDTO;
import com.example.mediconnect_project.Dto.RecetaDto;
import com.example.mediconnect_project.Dto.SolicitudNuevaRecetaDTO;
import com.example.mediconnect_project.Dto.SolicitudRenovacionDTO;
import com.example.mediconnect_project.Dto.SolicitudDto;
import com.example.mediconnect_project.Entity.MedicamentoEntity;
import com.example.mediconnect_project.Entity.MedicoEntity;
import com.example.mediconnect_project.Entity.PacienteEntity;
import com.example.mediconnect_project.Entity.PedidoEntity;
import com.example.mediconnect_project.Entity.RecetaEntity;
import com.example.mediconnect_project.Entity.SolicitudEntity;
import com.example.mediconnect_project.Entity.TratamientoEntity;
import com.example.mediconnect_project.Repository.MedicamentoRepository;
import com.example.mediconnect_project.Repository.MedicoRepository;
import com.example.mediconnect_project.Repository.PacienteRepository;
import com.example.mediconnect_project.Repository.PedidoRepository;
import com.example.mediconnect_project.Repository.RecetaRepository;
import com.example.mediconnect_project.Repository.SolicitudRepository;
import com.example.mediconnect_project.Repository.TratamientoRepository;
import com.example.mediconnect_project.Security.UsuarioActual;
import com.example.mediconnect_project.Service.AuditoriaService;
import com.example.mediconnect_project.Service.RecetaService;
import com.example.mediconnect_project.Service.SolicitudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SolicitudServiceImpl implements SolicitudService {

    // Valores de tm_solicitud.estado_solicitud
    public static final int ESTADO_PENDIENTE = 1;
    public static final int ESTADO_EN_REVISION = 2;
    public static final int ESTADO_APROBADA = 3;
    public static final int ESTADO_RECHAZADA = 4;

    private static final int ESTADO_ACTIVO = 1;
    public static final String TIPO_RENOVACION = "RENOVACION";
    public static final String TIPO_NUEVA_RECETA = "NUEVA_RECETA";
    private static final String MOTIVO_RENOVACION = "Renovación de receta";
    private static final String MOTIVO_NUEVA_RECETA = "Solicitud de nueva receta";

    @Autowired
    private SolicitudRepository repository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private RecetaService recetaService;

    @Autowired
    private AuditoriaService auditoriaService;

    public static String nombre_estado(Integer estado) {
        if (estado == null) {
            return "DESCONOCIDO";
        }
        switch (estado) {
            case ESTADO_PENDIENTE:
                return "PENDIENTE";
            case ESTADO_EN_REVISION:
                return "EN_REVISION";
            case ESTADO_APROBADA:
                return "APROBADA";
            case ESTADO_RECHAZADA:
                return "RECHAZADA";
            default:
                return "DESCONOCIDO";
        }
    }

    @Override
    @Transactional
    public SolicitudDto crear_nueva_receta(SolicitudNuevaRecetaDTO solicitud) {
        PacienteEntity paciente = paciente_actual();

        MedicoEntity medico = medicoRepository.findById(solicitud.getId_medico())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico no encontrado"));

        // Se quitan los medicamentos repetidos: un medicamento solo puede pedirse una vez por solicitud
        Set<Long> idMedicamentos = new LinkedHashSet<>(solicitud.getId_medicamentos());
        List<MedicamentoEntity> medicamentos = new ArrayList<>();
        for (Long idMedicamento : idMedicamentos) {
            medicamentos.add(medicamentoRepository.findById(idMedicamento)
                    .filter(m -> Integer.valueOf(ESTADO_ACTIVO).equals(m.getEstado_medicamento()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Medicamento no encontrado: " + idMedicamento)));
        }

        return registrar_solicitud(paciente, medico, TIPO_NUEVA_RECETA,
                solicitud.getMotivo() == null || solicitud.getMotivo().isBlank() ? MOTIVO_NUEVA_RECETA : solicitud.getMotivo(),
                medicamentos, null);
    }

    @Override
    @Transactional
    public SolicitudDto crear_renovacion(SolicitudRenovacionDTO solicitud) {
        PacienteEntity paciente = paciente_actual();

        RecetaEntity receta = recetaRepository.findById(solicitud.getId_receta())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta no encontrada"));

        // Solo se puede renovar una receta propia
        UsuarioActual.exigir_paciente(receta.getPaciente().getId_paciente());

        if (repository.existe_solicitud_abierta_para_receta(receta.getId_receta(), TIPO_RENOVACION,
                List.of(ESTADO_PENDIENTE, ESTADO_EN_REVISION))) {
            throw new IllegalArgumentException("Ya existe una solicitud de renovación en trámite para esta receta");
        }

        // La renovación pide los mismos medicamentos de la receta y va al médico que la emitió
        List<MedicamentoEntity> medicamentos = new ArrayList<>();
        for (TratamientoEntity tratamiento : tratamientoRepository.listar_por_receta(receta.getId_receta())) {
            medicamentos.add(tratamiento.getMedicamento());
        }
        if (medicamentos.isEmpty()) {
            throw new IllegalArgumentException("La receta no tiene medicamentos para renovar");
        }

        return registrar_solicitud(paciente, receta.getMedico(), TIPO_RENOVACION,
                solicitud.getMotivo() == null || solicitud.getMotivo().isBlank() ? MOTIVO_RENOVACION : solicitud.getMotivo(),
                medicamentos, receta);
    }

    // El paciente es quien tiene la sesión iniciada (solo un paciente puede solicitar)
    private PacienteEntity paciente_actual() {
        return pacienteRepository.findById(UsuarioActual.id_paciente())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    }

    // recetaOrigen: en una renovación es la receta que se renueva; en una receta nueva es null
    private SolicitudDto registrar_solicitud(PacienteEntity paciente, MedicoEntity medico, String tipo, String motivo,
                                             List<MedicamentoEntity> medicamentos, RecetaEntity recetaOrigen) {
        SolicitudEntity nueva = new SolicitudEntity();
        nueva.setPaciente(paciente);
        nueva.setMedico(medico);
        nueva.setReceta(recetaOrigen);
        nueva.setFecha_solicitud(LocalDate.now());
        nueva.setTipo_solicitud(tipo);
        nueva.setMotivo(motivo);
        nueva.setEstado_solicitud(ESTADO_PENDIENTE);
        nueva = repository.save(nueva);

        // La solicitud la crea el usuario del paciente
        Long idActor = paciente.getUsuario().getId_usuario();
        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_SOLICITUD, nueva.getId_solicitud(), idActor);

        for (MedicamentoEntity medicamento : medicamentos) {
            PedidoEntity pedido = new PedidoEntity();
            pedido.setSolicitud(nueva);
            pedido.setMedicamento(medicamento);
            pedido.setEstado_pedido(ESTADO_ACTIVO);
            pedido = pedidoRepository.save(pedido);
            auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_PEDIDO, pedido.getId_pedido(), idActor);
        }

        log.info("Solicitud {} ({}) del paciente {} para el médico {}", nueva.getId_solicitud(), tipo,
                paciente.getId_paciente(), medico.getId_medico());
        return convertir(nueva);
    }

    @Override
    public List<SolicitudDto> listar_por_paciente(Long idPaciente) {
        UsuarioActual.exigir_paciente(idPaciente);
        List<SolicitudEntity> listar = new ArrayList<>();
        listar = repository.listar_por_paciente(idPaciente);

        List<SolicitudDto> listarDto = new ArrayList<>();

        for (SolicitudEntity solicitudEntity : listar) {
            listarDto.add(convertir(solicitudEntity));
        }

        return listarDto;
    }

    @Override
    public List<SolicitudDto> listar_pendientes_medico(Long idMedico) {
        UsuarioActual.exigir_medico(idMedico);
        List<SolicitudEntity> listar = new ArrayList<>();
        listar = repository.listar_bandeja_medico(idMedico, List.of(ESTADO_PENDIENTE, ESTADO_EN_REVISION));

        List<SolicitudDto> listarDto = new ArrayList<>();

        for (SolicitudEntity solicitudEntity : listar) {
            listarDto.add(convertir(solicitudEntity));
        }

        return listarDto;
    }

    @Override
    public List<SolicitudDto> listar_por_medico(Long idMedico, Integer estado) {
        UsuarioActual.exigir_medico(idMedico);

        List<SolicitudEntity> listar = new ArrayList<>();
        if (estado == null) {
            listar = repository.listar_por_medico(idMedico);
        } else {
            if (estado < ESTADO_PENDIENTE || estado > ESTADO_RECHAZADA) {
                throw new IllegalArgumentException("El estado debe ser 1 (pendiente), 2 (en revisión), 3 (aprobada) o 4 (rechazada)");
            }
            listar = repository.listar_por_medico_y_estado(idMedico, estado);
        }

        List<SolicitudDto> listarDto = new ArrayList<>();

        for (SolicitudEntity solicitudEntity : listar) {
            listarDto.add(convertir(solicitudEntity));
        }

        return listarDto;
    }

    @Override
    @Transactional
    public SolicitudDto marcar_en_revision(Long idSolicitud) {
        SolicitudEntity solicitud = buscar_solicitud_del_medico(idSolicitud);

        if (solicitud.getEstado_solicitud() != ESTADO_PENDIENTE) {
            throw new IllegalArgumentException("Solo se puede pasar a revisión una solicitud pendiente");
        }

        solicitud.setEstado_solicitud(ESTADO_EN_REVISION);
        solicitud = repository.save(solicitud);
        auditoriaService.registrar_edicion(AuditoriaServiceImpl.TABLA_SOLICITUD, idSolicitud, idUsuarioMedico(solicitud));
        return convertir(solicitud);
    }

    @Override
    @Transactional
    public RecetaDto aprobar(Long idSolicitud, AprobacionDTO aprobacion) {
        SolicitudEntity solicitud = buscar_solicitud_del_medico(idSolicitud);
        validar_solicitud_abierta(solicitud);

        // Si falla la emisión (por ejemplo, el código de verificación) la transacción se revierte
        RecetaDto receta = recetaService.emitir(solicitud, aprobacion.getDetalles());

        // En una renovación se conserva la receta que se renovó; en una receta nueva se enlaza la receta emitida
        if (solicitud.getReceta() == null) {
            solicitud.setReceta(recetaRepository.getReferenceById(receta.getId_receta()));
        }
        solicitud.setEstado_solicitud(ESTADO_APROBADA);
        repository.save(solicitud);
        auditoriaService.registrar_edicion(AuditoriaServiceImpl.TABLA_SOLICITUD, idSolicitud, idUsuarioMedico(solicitud));

        log.info("Solicitud {} aprobada, receta {}", idSolicitud, receta.getId_receta());
        return receta;
    }

    @Override
    @Transactional
    public SolicitudDto rechazar(Long idSolicitud, RechazoDTO rechazo) {
        SolicitudEntity solicitud = buscar_solicitud_del_medico(idSolicitud);
        validar_solicitud_abierta(solicitud);

        solicitud.setMotivo_estado(rechazo.getMotivo_rechazo().trim());
        solicitud.setEstado_solicitud(ESTADO_RECHAZADA);
        solicitud = repository.save(solicitud);
        auditoriaService.registrar_edicion(AuditoriaServiceImpl.TABLA_SOLICITUD, idSolicitud, idUsuarioMedico(solicitud));
        return convertir(solicitud);
    }

    // Las acciones sobre una solicitud (revisar, aprobar, rechazar) las hace el usuario del médico asignado
    private Long idUsuarioMedico(SolicitudEntity solicitud) {
        return solicitud.getMedico().getUsuario().getId_usuario();
    }

    // Solo el médico al que se le asignó la solicitud puede atenderla
    private SolicitudEntity buscar_solicitud_del_medico(Long idSolicitud) {
        SolicitudEntity solicitud = buscar_solicitud(idSolicitud);
        UsuarioActual.exigir_medico(solicitud.getMedico().getId_medico());
        return solicitud;
    }

    private SolicitudEntity buscar_solicitud(Long idSolicitud) {
        return repository.findById(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    private void validar_solicitud_abierta(SolicitudEntity solicitud) {
        if (solicitud.getEstado_solicitud() != ESTADO_PENDIENTE
                && solicitud.getEstado_solicitud() != ESTADO_EN_REVISION) {
            throw new IllegalArgumentException("La solicitud ya fue atendida: " + nombre_estado(solicitud.getEstado_solicitud()));
        }
    }

    private SolicitudDto convertir(SolicitudEntity solicitud) {
        SolicitudDto dto = new SolicitudDto();

        dto.setId_solicitud(solicitud.getId_solicitud());
        dto.setFecha_solicitud(solicitud.getFecha_solicitud());
        dto.setTipo_solicitud(solicitud.getTipo_solicitud());
        dto.setMotivo(solicitud.getMotivo());
        dto.setMotivo_estado(solicitud.getMotivo_estado());
        dto.setEstado_solicitud(solicitud.getEstado_solicitud());
        dto.setEstado_nombre(nombre_estado(solicitud.getEstado_solicitud()));
        dto.setId_paciente(solicitud.getPaciente().getId_paciente());
        dto.setNombre_paciente(solicitud.getPaciente().getUsuario().getNombre_usuario() + " "
                + solicitud.getPaciente().getUsuario().getApellido_usuario());
        dto.setId_medico(solicitud.getMedico().getId_medico());
        dto.setNombre_medico(solicitud.getMedico().getUsuario().getNombre_usuario() + " "
                + solicitud.getMedico().getUsuario().getApellido_usuario());

        if (solicitud.getReceta() != null) {
            dto.setId_receta(solicitud.getReceta().getId_receta());
        }

        List<MedicamentoSolicitadoDto> medicamentos = new ArrayList<>();
        for (PedidoEntity pedido : pedidoRepository.listar_por_solicitud(solicitud.getId_solicitud())) {
            medicamentos.add(new MedicamentoSolicitadoDto(
                    pedido.getMedicamento().getId_medicamento(),
                    pedido.getMedicamento().getNombre_medicamento(),
                    pedido.getMedicamento().getConcentracion()));
        }
        dto.setMedicamentos(medicamentos);

        return dto;
    }
}
