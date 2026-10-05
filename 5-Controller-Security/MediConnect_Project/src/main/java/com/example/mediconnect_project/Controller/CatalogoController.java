package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.AlergiaDTO;
import com.example.mediconnect_project.Dto.EnfermedadDTO;
import com.example.mediconnect_project.Dto.MedicamentoDTO;
import com.example.mediconnect_project.Service.AlergiaService;
import com.example.mediconnect_project.Service.EnfermedadService;
import com.example.mediconnect_project.Service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("catalogo")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controlador de catálogos", description = "Permite listar y registrar alergias, enfermedades y medicamentos")
public class CatalogoController {

    @Autowired
    private AlergiaService alergiaService;

    @Autowired
    private EnfermedadService enfermedadService;

    @Autowired
    private MedicamentoService medicamentoService;

    @GetMapping("alergias")
    @Operation(summary = "Listar el catálogo de alergias")
    public List<AlergiaDTO> listar_alergias() {
        return alergiaService.listar();
    }

    @PostMapping("alergias")
    public AlergiaDTO guardar_alergia(@RequestBody AlergiaDTO alergia) {
        return alergiaService.guardar(alergia);
    }

    @GetMapping("enfermedades")
    @Operation(summary = "Listar el catálogo de enfermedades")
    public List<EnfermedadDTO> listar_enfermedades() {
        return enfermedadService.listar();
    }

    @PostMapping("enfermedades")
    public EnfermedadDTO guardar_enfermedad(@RequestBody EnfermedadDTO enfermedad) {
        return enfermedadService.guardar(enfermedad);
    }

    @GetMapping("medicamentos")
    @Operation(summary = "Listar el catálogo de medicamentos")
    public List<MedicamentoDTO> listar_medicamentos() {
        return medicamentoService.listar();
    }

    @PostMapping("medicamentos")
    public MedicamentoDTO guardar_medicamento(@RequestBody MedicamentoDTO medicamento) {
        return medicamentoService.guardar(medicamento);
    }

    @DeleteMapping("alergias/{idAlergia}")
    @Operation(summary = "Eliminar una alergia del catálogo (baja lógica)")
    public String eliminar_alergia(@PathVariable Long idAlergia) {
        alergiaService.eliminar(idAlergia);
        return "Alergia eliminada";
    }

    @DeleteMapping("enfermedades/{idEnfermedad}")
    @Operation(summary = "Eliminar una enfermedad del catálogo (baja lógica)")
    public String eliminar_enfermedad(@PathVariable Long idEnfermedad) {
        enfermedadService.eliminar(idEnfermedad);
        return "Enfermedad eliminada";
    }

    @DeleteMapping("medicamentos/{idMedicamento}")
    @Operation(summary = "Eliminar un medicamento del catálogo (baja lógica)")
    public String eliminar_medicamento(@PathVariable Long idMedicamento) {
        medicamentoService.eliminar(idMedicamento);
        return "Medicamento eliminado";
    }
}
