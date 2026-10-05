package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.MedicoDto;
import com.example.mediconnect_project.Entity.MedicoEntity;
import com.example.mediconnect_project.Repository.MedicoRepository;
import com.example.mediconnect_project.Service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MedicoServiceImpl implements MedicoService {

    @Autowired
    private MedicoRepository repository;

    @Override
    public List<MedicoDto> listar_medicos() {
        List<MedicoEntity> listar = new ArrayList<>();
        listar = repository.listar_activos();

        List<MedicoDto> listarDto = new ArrayList<>();

        for (MedicoEntity medicoEntity : listar) {

            MedicoDto medicoDto = new MedicoDto();

            medicoDto.setId_medico(medicoEntity.getId_medico());
            medicoDto.setNombre_medico(medicoEntity.getUsuario().getNombre_usuario() + " " + medicoEntity.getUsuario().getApellido_usuario());
            medicoDto.setEspecialidad(medicoEntity.getEspecialidad());
            medicoDto.setCmp(medicoEntity.getCmp());
            medicoDto.setCentro_atencion(medicoEntity.getCentro_atencion());

            listarDto.add(medicoDto);
        }

        return listarDto;
    }
}
