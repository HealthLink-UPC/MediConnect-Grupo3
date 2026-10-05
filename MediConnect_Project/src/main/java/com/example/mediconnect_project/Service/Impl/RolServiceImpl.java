package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Config.RolesIniciales;
import com.example.mediconnect_project.Dto.RolDTO;
import com.example.mediconnect_project.Entity.RolEntity;
import com.example.mediconnect_project.Repository.RolRepository;
import com.example.mediconnect_project.Service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolServiceImpl implements RolService {

    @Autowired
    private RolRepository repository;

    // Solo se listan los roles que se pueden elegir al registrarse
    @Override
    public List<RolDTO> listar_para_registro() {
        List<RolEntity> listar = new ArrayList<>();
        listar = repository.listar_activos_por_nombres(List.of(RolesIniciales.ROL_PACIENTE, RolesIniciales.ROL_MEDICO));

        List<RolDTO> listarDto = new ArrayList<>();

        for (RolEntity rolEntity : listar) {

            RolDTO rolDto = new RolDTO();

            rolDto.setId_rol(rolEntity.getId_rol());
            rolDto.setNombre_rol(rolEntity.getNombre_rol());
            rolDto.setEstado_rol(rolEntity.getEstado_rol());

            listarDto.add(rolDto);
        }

        return listarDto;
    }
}
