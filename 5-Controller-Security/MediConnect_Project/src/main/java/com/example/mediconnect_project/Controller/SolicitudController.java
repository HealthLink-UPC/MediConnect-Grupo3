package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.AprobacionDTO;
import com.example.mediconnect_project.Dto.RechazoDTO;
import com.example.mediconnect_project.Dto.RecetaDto;
import com.example.mediconnect_project.Dto.SolicitudNuevaRecetaDTO;
import com.example.mediconnect_project.Dto.SolicitudRenovacionDTO;
import com.example.mediconnect_project.Dto.SolicitudDto;
import com.example.mediconnect_project.Service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("solicitudes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controlador de solicitudes", description = "Permite solicitar la renovación de recetas y que el médico las apruebe o rechace")
public class SolicitudController {

    @Autowired
    private SolicitudService service;

    @PostMapping("nueva-receta")
    @Operation(summary = "El paciente solicita una receta nueva: elige el médico y los medicamentos (queda en estado PENDIENTE)")
    public SolicitudDto crear_nueva_receta(@Valid @RequestBody SolicitudNuevaRecetaDTO solicitud) {
        return service.crear_nueva_receta(solicitud);
    }

    @PostMapping("renovacion")
    @Operation(summary = "El paciente solicita renovar una receta suya: indica la receta y va al médico que la emitió (queda en estado PENDIENTE)")
    public SolicitudDto crear_renovacion(@Valid @RequestBody SolicitudRenovacionDTO solicitud) {
        return service.crear_renovacion(solicitud);
    }

    @GetMapping("paciente/{idPaciente}")
    @Operation(summary = "Listar las solicitudes del paciente con su estado")
    public List<SolicitudDto> listar_por_paciente(@PathVariable Long idPaciente) {
        return service.listar_por_paciente(idPaciente);
    }

    @GetMapping("medico/{idMedico}")
    @Operation(summary = "El médico consulta todas sus solicitudes (de la más reciente a la más antigua); estado opcional: 1 pendiente, 2 en revisión, 3 aprobada, 4 rechazada")
    public List<SolicitudDto> listar_por_medico(@PathVariable Long idMedico,
                                                @RequestParam(required = false) Integer estado) {
        return service.listar_por_medico(idMedico, estado);
    }

    @GetMapping("medico/{idMedico}/pendientes")
    @Operation(summary = "Bandeja del médico: solicitudes pendientes o en revisión, de la más antigua a la más reciente")
    public List<SolicitudDto> listar_pendientes(@PathVariable Long idMedico) {
        return service.listar_pendientes_medico(idMedico);
    }

    @PutMapping("{idSolicitud}/revision")
    @Operation(summary = "El médico pasa la solicitud a estado EN_REVISION")
    public SolicitudDto marcar_en_revision(@PathVariable Long idSolicitud) {
        return service.marcar_en_revision(idSolicitud);
    }

    @PutMapping("{idSolicitud}/aprobar")
    @Operation(summary = "El médico aprueba la solicitud y se emite la receta con su código de verificación")
    public RecetaDto aprobar(@PathVariable Long idSolicitud, @Valid @RequestBody AprobacionDTO aprobacion) {
        return service.aprobar(idSolicitud, aprobacion);
    }

    @PutMapping("{idSolicitud}/rechazar")
    @Operation(summary = "El médico rechaza la solicitud indicando el motivo (obligatorio)")
    public SolicitudDto rechazar(@PathVariable Long idSolicitud, @Valid @RequestBody RechazoDTO rechazo) {
        return service.rechazar(idSolicitud, rechazo);
    }
}
