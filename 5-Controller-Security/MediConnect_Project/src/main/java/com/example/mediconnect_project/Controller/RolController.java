package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.RolDTO;
import com.example.mediconnect_project.Service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("roles")
@Tag(name = "Controlador de roles", description = "Permite consultar los roles disponibles para el registro")
public class RolController {

    @Autowired
    private RolService service;

    @GetMapping("listar")
    @Operation(summary = "Listar los roles que se pueden elegir al registrarse, con su id")
    public List<RolDTO> listar() {
        return service.listar_para_registro();
    }
}
