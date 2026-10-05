package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.AuditoriaDTO;
import com.example.mediconnect_project.Service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("auditoria")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controlador de auditoría", description = "Permite consultar quién creó, editó o eliminó cada registro y cuándo")
public class AuditoriaController {

    @Autowired
    private AuditoriaService service;

    @GetMapping("listar")
    @Operation(summary = "Listar los registros de auditoría (tabla, id del registro, usuario y fechas)")
    public List<AuditoriaDTO> listar() {
        return service.listar();
    }
}
