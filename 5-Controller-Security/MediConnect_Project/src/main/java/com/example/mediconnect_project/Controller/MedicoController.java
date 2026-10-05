package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.MedicoDto;
import com.example.mediconnect_project.Service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("medicos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controlador de médico", description = "Permite listar los médicos disponibles para enviar una solicitud")
public class MedicoController {

    @Autowired
    private MedicoService service;

    @GetMapping("listar")
    @Operation(summary = "Listar los médicos registrados")
    public List<MedicoDto> listar() {
        return service.listar_medicos();
    }
}
