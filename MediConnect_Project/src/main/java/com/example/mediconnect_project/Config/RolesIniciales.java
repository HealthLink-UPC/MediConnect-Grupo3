package com.example.mediconnect_project.Config;

import com.example.mediconnect_project.Entity.RolEntity;
import com.example.mediconnect_project.Repository.RolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * El script de la base no inserta datos: al arrancar se crean los roles del registro público
 * si todavía no existen (PACIENTE primero y MEDICO después).
 */
@Slf4j
@Component
public class RolesIniciales implements CommandLineRunner {

    public static final String ROL_PACIENTE = "PACIENTE";
    public static final String ROL_MEDICO = "MEDICO";

    @Autowired
    private RolRepository rolRepository;

    @Override
    public void run(String... args) {
        for (String nombre : List.of(ROL_PACIENTE, ROL_MEDICO)) {
            if (rolRepository.buscar_por_nombre(nombre).isEmpty()) {
                RolEntity rol = new RolEntity();
                rol.setNombre_rol(nombre);
                rol.setEstado_rol(1);
                rolRepository.save(rol);
                log.info("Rol creado: {}", nombre);
            }
        }
    }
}
