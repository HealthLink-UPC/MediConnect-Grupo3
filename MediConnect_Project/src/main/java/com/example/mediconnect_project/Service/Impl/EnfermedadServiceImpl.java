package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.EnfermedadDTO;
import com.example.mediconnect_project.Entity.EnfermedadEntity;
import com.example.mediconnect_project.Repository.EnfermedadRepository;
import com.example.mediconnect_project.Security.UsuarioActual;
import com.example.mediconnect_project.Service.AuditoriaService;
import com.example.mediconnect_project.Service.EnfermedadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class EnfermedadServiceImpl implements EnfermedadService {

    @Autowired
    private EnfermedadRepository repository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Override
    public List<EnfermedadDTO> listar() {
        List<EnfermedadEntity> listar = new ArrayList<>();
        listar = repository.listar_activas();

        List<EnfermedadDTO> listarDto = new ArrayList<>();

        for (EnfermedadEntity enfermedadEntity : listar) {
            listarDto.add(convertir(enfermedadEntity));
        }

        return listarDto;
    }

    @Override
    public EnfermedadDTO guardar(EnfermedadDTO enfermedad) {
        if (enfermedad.getNombre_enfermedad() == null || enfermedad.getNombre_enfermedad().isBlank()) {
            throw new IllegalArgumentException("El nombre de la enfermedad es obligatorio");
        }
        if (enfermedad.getNombre_enfermedad().length() > 100) {
            throw new IllegalArgumentException("El nombre de la enfermedad no puede superar los 100 caracteres");
        }
        if (repository.existe_nombre(enfermedad.getNombre_enfermedad().trim())) {
            throw new IllegalArgumentException("La enfermedad ya se encuentra registrada");
        }

        EnfermedadEntity nueva = new EnfermedadEntity();
        nueva.setNombre_enfermedad(enfermedad.getNombre_enfermedad().trim());
        nueva.setEstado_enfermedad(1);

        nueva = repository.save(nueva);
        // La registra el usuario que tiene la sesión iniciada
        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_ENFERMEDAD, nueva.getId_enfermedad(), UsuarioActual.id_usuario());

        return convertir(nueva);
    }

    @Override
    @Transactional
    public void eliminar(Long idEnfermedad) {
        EnfermedadEntity registro = repository.findById(idEnfermedad)
                .filter(r -> Integer.valueOf(1).equals(r.getEstado_enfermedad()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enfermedad no encontrada"));

        registro.setEstado_enfermedad(0);
        repository.save(registro);
        // La elimina el usuario que tiene la sesión iniciada
        auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_ENFERMEDAD, registro.getId_enfermedad(), UsuarioActual.id_usuario());
    }

    private EnfermedadDTO convertir(EnfermedadEntity enfermedad) {
        EnfermedadDTO dto = new EnfermedadDTO();
        dto.setId_enfermedad(enfermedad.getId_enfermedad());
        dto.setNombre_enfermedad(enfermedad.getNombre_enfermedad());
        dto.setEstado_enfermedad(enfermedad.getEstado_enfermedad());
        return dto;
    }
}
