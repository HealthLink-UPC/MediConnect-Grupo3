package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.RecetaDto;
import com.example.mediconnect_project.Service.RecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("recetas")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controlador de recetas", description = "Permite consultar el historial de recetas del paciente y el detalle de cada una")
public class RecetaController {

    @Autowired
    private RecetaService service;

    @GetMapping("paciente/{idPaciente}")
    @Operation(summary = "Historial de recetas del paciente, de la más reciente a la más antigua")
    public List<RecetaDto> historial(@PathVariable Long idPaciente) {
        return service.listar_historial(idPaciente);
    }

    @GetMapping("{idReceta}")
    @Operation(summary = "Detalle de una receta con sus medicamentos")
    public RecetaDto detalle(@PathVariable Long idReceta) {
        return service.obtener_detalle(idReceta);
    }

}
