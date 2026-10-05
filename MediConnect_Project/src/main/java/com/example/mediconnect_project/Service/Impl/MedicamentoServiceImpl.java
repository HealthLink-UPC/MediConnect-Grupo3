package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.MedicamentoDTO;
import com.example.mediconnect_project.Entity.MedicamentoEntity;
import com.example.mediconnect_project.Repository.MedicamentoRepository;
import com.example.mediconnect_project.Security.UsuarioActual;
import com.example.mediconnect_project.Service.AuditoriaService;
import com.example.mediconnect_project.Service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoRepository repository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Override
    public List<MedicamentoDTO> listar() {
        List<MedicamentoEntity> listar = new ArrayList<>();
        listar = repository.listar_activos();

        List<MedicamentoDTO> listarDto = new ArrayList<>();

        for (MedicamentoEntity medicamentoEntity : listar) {
            listarDto.add(convertir(medicamentoEntity));
        }

        return listarDto;
    }

    @Override
    public MedicamentoDTO guardar(MedicamentoDTO medicamento) {
        if (medicamento.getNombre_medicamento() == null || medicamento.getNombre_medicamento().isBlank()
                || medicamento.getConcentracion() == null || medicamento.getConcentracion().isBlank()) {
            throw new IllegalArgumentException("El nombre y la concentración del medicamento son obligatorios");
        }
        if (medicamento.getNombre_medicamento().length() > 100 || medicamento.getConcentracion().length() > 50) {
            throw new IllegalArgumentException("El nombre (máx. 100) o la concentración (máx. 50) superan la longitud permitida");
        }
        if (repository.existe_medicamento(medicamento.getNombre_medicamento().trim(), medicamento.getConcentracion().trim())) {
            throw new IllegalArgumentException("El medicamento ya se encuentra registrado");
        }

        MedicamentoEntity nuevo = new MedicamentoEntity();
        nuevo.setNombre_medicamento(medicamento.getNombre_medicamento().trim());
        nuevo.setConcentracion(medicamento.getConcentracion().trim());
        nuevo.setEstado_medicamento(1);

        nuevo = repository.save(nuevo);
        // La registra el usuario que tiene la sesión iniciada
        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_MEDICAMENTO, nuevo.getId_medicamento(), UsuarioActual.id_usuario());

        return convertir(nuevo);
    }

    @Override
    @Transactional
    public void eliminar(Long idMedicamento) {
        MedicamentoEntity registro = repository.findById(idMedicamento)
                .filter(r -> Integer.valueOf(1).equals(r.getEstado_medicamento()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento no encontrado"));

        registro.setEstado_medicamento(0);
        repository.save(registro);
        // La elimina el usuario que tiene la sesión iniciada
        auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_MEDICAMENTO, registro.getId_medicamento(), UsuarioActual.id_usuario());
    }

    private MedicamentoDTO convertir(MedicamentoEntity medicamento) {
        MedicamentoDTO dto = new MedicamentoDTO();
        dto.setId_medicamento(medicamento.getId_medicamento());
        dto.setNombre_medicamento(medicamento.getNombre_medicamento());
        dto.setConcentracion(medicamento.getConcentracion());
        dto.setEstado_medicamento(medicamento.getEstado_medicamento());
        return dto;
    }
}
