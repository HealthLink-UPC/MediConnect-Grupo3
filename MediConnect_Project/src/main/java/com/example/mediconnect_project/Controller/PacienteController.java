package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.DatosClinicosDTO;
import com.example.mediconnect_project.Dto.FichaClinicaDto;
import com.example.mediconnect_project.Dto.PacienteAlergiaDTO;
import com.example.mediconnect_project.Dto.PacienteEnfermedadDTO;
import com.example.mediconnect_project.Service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pacientes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controlador de paciente", description = "Permite registrar y consultar la ficha clínica del paciente")
public class PacienteController {

    @Autowired
    private PacienteService service;

    @PutMapping("{idPaciente}/datos-clinicos")
    @Operation(summary = "Registrar tipo de sangre, peso y talla del paciente")
    public FichaClinicaDto guardar_datos_clinicos(@PathVariable Long idPaciente,
                                                  @Valid @RequestBody DatosClinicosDTO datos) {
        return service.guardar_datos_clinicos(idPaciente, datos);
    }

    @PostMapping("{idPaciente}/alergias")
    @Operation(summary = "Registrar una alergia en la ficha del paciente")
    public FichaClinicaDto agregar_alergia(@PathVariable Long idPaciente,
                                           @Valid @RequestBody PacienteAlergiaDTO alergia) {
        return service.agregar_alergia(idPaciente, alergia);
    }

    @PostMapping("{idPaciente}/enfermedades")
    @Operation(summary = "Registrar una condición preexistente en la ficha del paciente")
    public FichaClinicaDto agregar_enfermedad(@PathVariable Long idPaciente,
                                              @Valid @RequestBody PacienteEnfermedadDTO enfermedad) {
        return service.agregar_enfermedad(idPaciente, enfermedad);
    }

    @DeleteMapping("{idPaciente}/alergias/{idAlergia}")
    @Operation(summary = "Quitar una alergia de la ficha del paciente (baja lógica)")
    public FichaClinicaDto quitar_alergia(@PathVariable Long idPaciente, @PathVariable Long idAlergia) {
        return service.quitar_alergia(idPaciente, idAlergia);
    }

    @DeleteMapping("{idPaciente}/enfermedades/{idEnfermedad}")
    @Operation(summary = "Quitar una condición preexistente de la ficha del paciente (baja lógica)")
    public FichaClinicaDto quitar_enfermedad(@PathVariable Long idPaciente, @PathVariable Long idEnfermedad) {
        return service.quitar_enfermedad(idPaciente, idEnfermedad);
    }

    @GetMapping("{idPaciente}/ficha")
    @Operation(summary = "Consultar la ficha clínica consolidada del paciente")
    public FichaClinicaDto ficha(@PathVariable Long idPaciente) {
        return service.obtener_ficha(idPaciente);
    }
}
