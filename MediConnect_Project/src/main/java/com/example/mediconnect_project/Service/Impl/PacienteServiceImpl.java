package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.DatosClinicosDTO;
import com.example.mediconnect_project.Dto.FichaClinicaDto;
import com.example.mediconnect_project.Dto.PacienteAlergiaDTO;
import com.example.mediconnect_project.Dto.PacienteEnfermedadDTO;
import com.example.mediconnect_project.Entity.AlergiaEntity;
import com.example.mediconnect_project.Entity.DiagnosticoEntity;
import com.example.mediconnect_project.Entity.EnfermedadEntity;
import com.example.mediconnect_project.Entity.PacienteEntity;
import com.example.mediconnect_project.Entity.SensibilidadEntity;
import com.example.mediconnect_project.Repository.AlergiaRepository;
import com.example.mediconnect_project.Repository.DiagnosticoRepository;
import com.example.mediconnect_project.Repository.EnfermedadRepository;
import com.example.mediconnect_project.Repository.PacienteRepository;
import com.example.mediconnect_project.Repository.SensibilidadRepository;
import com.example.mediconnect_project.Security.UsuarioActual;
import com.example.mediconnect_project.Service.AuditoriaService;
import com.example.mediconnect_project.Service.PacienteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PacienteServiceImpl implements PacienteService {

    public static final int ESTADO_ACTIVO = 1;
    public static final int ESTADO_INACTIVO = 0;

    @Autowired
    private PacienteRepository repository;

    @Autowired
    private AlergiaRepository alergiaRepository;

    @Autowired
    private EnfermedadRepository enfermedadRepository;

    @Autowired
    private SensibilidadRepository sensibilidadRepository;

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Override
    @Transactional
    public FichaClinicaDto guardar_datos_clinicos(Long idPaciente, DatosClinicosDTO datos) {
        UsuarioActual.exigir_paciente(idPaciente);
        PacienteEntity paciente = buscar_paciente(idPaciente);

        paciente.setTipo_sangre(datos.getTipo_sangre());
        paciente.setPeso(datos.getPeso());
        paciente.setTalla(datos.getTalla());
        repository.save(paciente);
        auditoriaService.registrar_edicion(AuditoriaServiceImpl.TABLA_PACIENTE, idPaciente, paciente.getUsuario().getId_usuario());

        return obtener_ficha(idPaciente);
    }

    @Override
    @Transactional
    public FichaClinicaDto agregar_alergia(Long idPaciente, PacienteAlergiaDTO alergia) {
        UsuarioActual.exigir_paciente(idPaciente);
        PacienteEntity paciente = buscar_paciente(idPaciente);

        AlergiaEntity alergiaEntity = alergiaRepository.findById(alergia.getId_alergia())
                .filter(a -> Integer.valueOf(ESTADO_ACTIVO).equals(a.getEstado_alergia()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia no encontrada"));

        // La base no permite repetir (paciente, alergia): si existe inactiva se reactiva
        Optional<SensibilidadEntity> existente = sensibilidadRepository.buscar_registro(idPaciente, alergia.getId_alergia());
        if (existente.isPresent()) {
            if (Integer.valueOf(ESTADO_ACTIVO).equals(existente.get().getEstado_sensibilidad())) {
                throw new IllegalArgumentException("La alergia ya está registrada en la ficha del paciente");
            }
            existente.get().setEstado_sensibilidad(ESTADO_ACTIVO);
            sensibilidadRepository.save(existente.get());
            auditoriaService.registrar_edicion(AuditoriaServiceImpl.TABLA_SENSIBILIDAD, existente.get().getId_sensibilidad(),
                    paciente.getUsuario().getId_usuario());
        } else {
            SensibilidadEntity sensibilidad = new SensibilidadEntity();
            sensibilidad.setPaciente(paciente);
            sensibilidad.setAlergia(alergiaEntity);
            sensibilidad.setEstado_sensibilidad(ESTADO_ACTIVO);
            sensibilidad = sensibilidadRepository.save(sensibilidad);
            auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_SENSIBILIDAD, sensibilidad.getId_sensibilidad(),
                    paciente.getUsuario().getId_usuario());
        }

        return obtener_ficha(idPaciente);
    }

    @Override
    @Transactional
    public FichaClinicaDto agregar_enfermedad(Long idPaciente, PacienteEnfermedadDTO enfermedad) {
        UsuarioActual.exigir_paciente(idPaciente);
        PacienteEntity paciente = buscar_paciente(idPaciente);

        EnfermedadEntity enfermedadEntity = enfermedadRepository.findById(enfermedad.getId_enfermedad())
                .filter(e -> Integer.valueOf(ESTADO_ACTIVO).equals(e.getEstado_enfermedad()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enfermedad no encontrada"));

        // La base no permite repetir (paciente, enfermedad): si existe inactiva se reactiva
        Optional<DiagnosticoEntity> existente = diagnosticoRepository.buscar_registro(idPaciente, enfermedad.getId_enfermedad());
        if (existente.isPresent()) {
            if (Integer.valueOf(ESTADO_ACTIVO).equals(existente.get().getEstado_diagnostico())) {
                throw new IllegalArgumentException("La enfermedad ya está registrada en la ficha del paciente");
            }
            existente.get().setEstado_diagnostico(ESTADO_ACTIVO);
            diagnosticoRepository.save(existente.get());
            auditoriaService.registrar_edicion(AuditoriaServiceImpl.TABLA_DIAGNOSTICO, existente.get().getId_diagnostico(),
                    paciente.getUsuario().getId_usuario());
        } else {
            DiagnosticoEntity diagnostico = new DiagnosticoEntity();
            diagnostico.setPaciente(paciente);
            diagnostico.setEnfermedad(enfermedadEntity);
            diagnostico.setEstado_diagnostico(ESTADO_ACTIVO);
            diagnostico = diagnosticoRepository.save(diagnostico);
            auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_DIAGNOSTICO, diagnostico.getId_diagnostico(),
                    paciente.getUsuario().getId_usuario());
        }

        return obtener_ficha(idPaciente);
    }

    @Override
    @Transactional
    public FichaClinicaDto quitar_alergia(Long idPaciente, Long idAlergia) {
        UsuarioActual.exigir_paciente(idPaciente);
        PacienteEntity paciente = buscar_paciente(idPaciente);

        SensibilidadEntity sensibilidad = sensibilidadRepository.buscar_registro(idPaciente, idAlergia)
                .filter(s -> Integer.valueOf(ESTADO_ACTIVO).equals(s.getEstado_sensibilidad()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La alergia no está registrada en la ficha del paciente"));

        sensibilidad.setEstado_sensibilidad(ESTADO_INACTIVO);
        sensibilidadRepository.save(sensibilidad);
        auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_SENSIBILIDAD, sensibilidad.getId_sensibilidad(),
                paciente.getUsuario().getId_usuario());

        return obtener_ficha(idPaciente);
    }

    @Override
    @Transactional
    public FichaClinicaDto quitar_enfermedad(Long idPaciente, Long idEnfermedad) {
        UsuarioActual.exigir_paciente(idPaciente);
        PacienteEntity paciente = buscar_paciente(idPaciente);

        DiagnosticoEntity diagnostico = diagnosticoRepository.buscar_registro(idPaciente, idEnfermedad)
                .filter(d -> Integer.valueOf(ESTADO_ACTIVO).equals(d.getEstado_diagnostico()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La enfermedad no está registrada en la ficha del paciente"));

        diagnostico.setEstado_diagnostico(ESTADO_INACTIVO);
        diagnosticoRepository.save(diagnostico);
        auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_DIAGNOSTICO, diagnostico.getId_diagnostico(),
                paciente.getUsuario().getId_usuario());

        return obtener_ficha(idPaciente);
    }

    @Override
    public FichaClinicaDto obtener_ficha(Long idPaciente) {
        log.info("Consultando ficha clínica del paciente {}", idPaciente);
        UsuarioActual.exigir_paciente_o_medico(idPaciente);
        PacienteEntity paciente = buscar_paciente(idPaciente);

        FichaClinicaDto ficha = new FichaClinicaDto();
        ficha.setId_paciente(paciente.getId_paciente());
        ficha.setNombre_paciente(paciente.getUsuario().getNombre_usuario() + " " + paciente.getUsuario().getApellido_usuario());

        // Los valores de "sin registrar" no se muestran como datos reales
        if (!UsuarioServiceImpl.SANGRE_SIN_REGISTRAR.equals(paciente.getTipo_sangre())) {
            ficha.setTipo_sangre(paciente.getTipo_sangre());
        }
        if (paciente.getPeso() != null && paciente.getPeso() > UsuarioServiceImpl.MEDIDA_SIN_REGISTRAR) {
            ficha.setPeso(paciente.getPeso());
        }
        if (paciente.getTalla() != null && paciente.getTalla() > UsuarioServiceImpl.MEDIDA_SIN_REGISTRAR) {
            ficha.setTalla(paciente.getTalla());
        }

        List<String> alergias = new ArrayList<>();
        for (SensibilidadEntity sensibilidad : sensibilidadRepository.listar_activas_por_paciente(idPaciente)) {
            alergias.add(sensibilidad.getAlergia().getNombre_alergia());
        }

        List<String> enfermedades = new ArrayList<>();
        for (DiagnosticoEntity diagnostico : diagnosticoRepository.listar_activos_por_paciente(idPaciente)) {
            enfermedades.add(diagnostico.getEnfermedad().getNombre_enfermedad());
        }

        ficha.setAlergias(alergias);
        ficha.setEnfermedades(enfermedades);

        if (alergias.isEmpty()) {
            ficha.setMensaje("No se registran alergias reportadas");
        }

        return ficha;
    }

    private PacienteEntity buscar_paciente(Long idPaciente) {
        return repository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    }
}
