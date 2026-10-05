package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.DetalleRecetaDto;
import com.example.mediconnect_project.Dto.ItemRecetaDTO;
import com.example.mediconnect_project.Dto.RecetaDto;
import com.example.mediconnect_project.Entity.MedicamentoEntity;
import com.example.mediconnect_project.Entity.RecetaEntity;
import com.example.mediconnect_project.Entity.SolicitudEntity;
import com.example.mediconnect_project.Entity.TratamientoEntity;
import com.example.mediconnect_project.Repository.MedicamentoRepository;
import com.example.mediconnect_project.Repository.RecetaRepository;
import com.example.mediconnect_project.Repository.TratamientoRepository;
import com.example.mediconnect_project.Security.UsuarioActual;
import com.example.mediconnect_project.Service.AuditoriaService;
import com.example.mediconnect_project.Service.RecetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class RecetaServiceImpl implements RecetaService {

    // Valores de tm_receta.estado_receta
    public static final int ESTADO_VIGENTE = 1;
    public static final int ESTADO_CADUCADA = 2;
    public static final int ESTADO_SURTIDA = 3;

    private static final int ESTADO_ACTIVO = 1;
    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD_CODIGO = 10;
    private static final int INTENTOS_CODIGO = 10;

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private RecetaRepository repository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public static String nombre_estado(Integer estado) {
        if (estado == null) {
            return "DESCONOCIDO";
        }
        switch (estado) {
            case ESTADO_VIGENTE:
                return "VIGENTE";
            case ESTADO_CADUCADA:
                return "CADUCADA";
            case ESTADO_SURTIDA:
                return "SURTIDA";
            default:
                return "DESCONOCIDO";
        }
    }

    @Override
    @Transactional
    public RecetaDto emitir(SolicitudEntity solicitud, List<ItemRecetaDTO> items) {
        LocalDate fechaVencimiento = null;
        Set<Long> medicamentosVistos = new HashSet<>();

        for (ItemRecetaDTO item : items) {
            if (!medicamentosVistos.add(item.getId_medicamento())) {
                throw new IllegalArgumentException("Una receta no puede incluir el mismo medicamento dos veces");
            }
            if (item.getFecha_fin().isBefore(item.getFecha_inicio())) {
                throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
            }
            if (fechaVencimiento == null || item.getFecha_fin().isAfter(fechaVencimiento)) {
                fechaVencimiento = item.getFecha_fin();
            }
        }

        RecetaEntity receta = new RecetaEntity();
        receta.setPaciente(solicitud.getPaciente());
        receta.setMedico(solicitud.getMedico());
        receta.setFecha_emision(LocalDate.now());
        receta.setFecha_vencimiento(fechaVencimiento);
        receta.setCodigo_verificacion(generar_codigo());
        receta.setEstado_receta(ESTADO_VIGENTE);
        receta = repository.save(receta);

        // La receta la emite el usuario del médico
        Long idActor = solicitud.getMedico().getUsuario().getId_usuario();
        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_RECETA, receta.getId_receta(), idActor);

        for (ItemRecetaDTO item : items) {
            MedicamentoEntity medicamento = medicamentoRepository.findById(item.getId_medicamento())
                    .filter(m -> Integer.valueOf(ESTADO_ACTIVO).equals(m.getEstado_medicamento()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Medicamento no encontrado: " + item.getId_medicamento()));

            TratamientoEntity tratamiento = new TratamientoEntity();
            tratamiento.setReceta(receta);
            tratamiento.setMedicamento(medicamento);
            tratamiento.setDosis(item.getDosis());
            tratamiento.setFrecuencia(item.getFrecuencia());
            tratamiento.setFecha_inicio(item.getFecha_inicio());
            tratamiento.setFecha_fin(item.getFecha_fin());
            tratamiento.setEstado_tratamiento(ESTADO_ACTIVO);
            tratamiento = tratamientoRepository.save(tratamiento);
            auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_TRATAMIENTO, tratamiento.getId_tratamiento(), idActor);
        }

        return convertir(receta, true);
    }

    @Override
    public List<RecetaDto> listar_historial(Long idPaciente) {
        UsuarioActual.exigir_paciente_o_medico(idPaciente);
        List<RecetaEntity> listar = new ArrayList<>();
        listar = repository.listar_por_paciente(idPaciente);

        List<RecetaDto> listarDto = new ArrayList<>();

        for (RecetaEntity recetaEntity : listar) {
            listarDto.add(convertir(recetaEntity, false));
        }

        return listarDto;
    }

    @Override
    public RecetaDto obtener_detalle(Long idReceta) {
        RecetaEntity receta = repository.findById(idReceta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta no encontrada"));

        // La ve el paciente dueño de la receta o cualquier médico
        UsuarioActual.exigir_paciente_o_medico(receta.getPaciente().getId_paciente());

        return convertir(receta, true);
    }

    private String generar_codigo() {
        for (int intento = 0; intento < INTENTOS_CODIGO; intento++) {
            StringBuilder codigo = new StringBuilder();
            for (int i = 0; i < LONGITUD_CODIGO; i++) {
                codigo.append(CARACTERES_CODIGO.charAt(random.nextInt(CARACTERES_CODIGO.length())));
            }
            if (!repository.existe_codigo(codigo.toString())) {
                return codigo.toString();
            }
        }
        throw new IllegalStateException("No se pudo generar el código de verificación, la receta no fue emitida");
    }

    private RecetaDto convertir(RecetaEntity receta, boolean incluirDetalles) {
        RecetaDto dto = new RecetaDto();

        dto.setId_receta(receta.getId_receta());
        dto.setFecha_emision(receta.getFecha_emision());
        dto.setFecha_vencimiento(receta.getFecha_vencimiento());
        dto.setCodigo_verificacion(receta.getCodigo_verificacion());
        dto.setEstado_receta(receta.getEstado_receta());
        dto.setEstado_nombre(nombre_estado(receta.getEstado_receta()));
        dto.setId_paciente(receta.getPaciente().getId_paciente());
        dto.setNombre_paciente(receta.getPaciente().getUsuario().getNombre_usuario() + " "
                + receta.getPaciente().getUsuario().getApellido_usuario());
        dto.setId_medico(receta.getMedico().getId_medico());
        dto.setNombre_medico(receta.getMedico().getUsuario().getNombre_usuario() + " "
                + receta.getMedico().getUsuario().getApellido_usuario());

        if (incluirDetalles) {
            List<DetalleRecetaDto> detalles = new ArrayList<>();

            for (TratamientoEntity tratamientoEntity : tratamientoRepository.listar_por_receta(receta.getId_receta())) {
                DetalleRecetaDto detalleDto = new DetalleRecetaDto();

                detalleDto.setId_tratamiento(tratamientoEntity.getId_tratamiento());
                detalleDto.setNombre_medicamento(tratamientoEntity.getMedicamento().getNombre_medicamento());
                detalleDto.setConcentracion(tratamientoEntity.getMedicamento().getConcentracion());
                detalleDto.setDosis(tratamientoEntity.getDosis());
                detalleDto.setFrecuencia(tratamientoEntity.getFrecuencia());
                detalleDto.setFecha_inicio(tratamientoEntity.getFecha_inicio());
                detalleDto.setFecha_fin(tratamientoEntity.getFecha_fin());

                detalles.add(detalleDto);
            }

            dto.setDetalles(detalles);
        }

        return dto;
    }
}
