package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.AlergiaDTO;
import com.example.mediconnect_project.Entity.AlergiaEntity;
import com.example.mediconnect_project.Repository.AlergiaRepository;
import com.example.mediconnect_project.Security.UsuarioActual;
import com.example.mediconnect_project.Service.AlergiaService;
import com.example.mediconnect_project.Service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlergiaServiceImpl implements AlergiaService {

    @Autowired
    private AlergiaRepository repository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Override
    public List<AlergiaDTO> listar() {
        List<AlergiaEntity> listar = new ArrayList<>();
        listar = repository.listar_activas();

        List<AlergiaDTO> listarDto = new ArrayList<>();

        for (AlergiaEntity alergiaEntity : listar) {
            listarDto.add(convertir(alergiaEntity));
        }

        return listarDto;
    }

    @Override
    public AlergiaDTO guardar(AlergiaDTO alergia) {
        if (alergia.getNombre_alergia() == null || alergia.getNombre_alergia().isBlank()) {
            throw new IllegalArgumentException("El nombre de la alergia es obligatorio");
        }
        if (alergia.getNombre_alergia().length() > 100) {
            throw new IllegalArgumentException("El nombre de la alergia no puede superar los 100 caracteres");
        }
        if (repository.existe_nombre(alergia.getNombre_alergia().trim())) {
            throw new IllegalArgumentException("La alergia ya se encuentra registrada");
        }

        AlergiaEntity nueva = new AlergiaEntity();
        nueva.setNombre_alergia(alergia.getNombre_alergia().trim());
        nueva.setEstado_alergia(1);

        nueva = repository.save(nueva);
        // La registra el usuario que tiene la sesión iniciada
        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_ALERGIA, nueva.getId_alergia(), UsuarioActual.id_usuario());

        return convertir(nueva);
    }

    @Override
    @Transactional
    public void eliminar(Long idAlergia) {
        AlergiaEntity registro = repository.findById(idAlergia)
                .filter(r -> Integer.valueOf(1).equals(r.getEstado_alergia()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia no encontrada"));

        registro.setEstado_alergia(0);
        repository.save(registro);
        // La elimina el usuario que tiene la sesión iniciada
        auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_ALERGIA, registro.getId_alergia(), UsuarioActual.id_usuario());
    }

    private AlergiaDTO convertir(AlergiaEntity alergia) {
        AlergiaDTO dto = new AlergiaDTO();
        dto.setId_alergia(alergia.getId_alergia());
        dto.setNombre_alergia(alergia.getNombre_alergia());
        dto.setEstado_alergia(alergia.getEstado_alergia());
        return dto;
    }
}
