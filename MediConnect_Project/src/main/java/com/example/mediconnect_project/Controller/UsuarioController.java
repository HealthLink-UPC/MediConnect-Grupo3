package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.PerfilDto;
import com.example.mediconnect_project.Service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controlador de usuario", description = "Permite consultar y editar el perfil del usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping("{idUsuario}")
    @Operation(summary = "Consultar el perfil del usuario")
    public PerfilDto perfil(@PathVariable Long idUsuario) {
        return service.obtener_perfil(idUsuario);
    }

    @DeleteMapping("{idUsuario}")
    @Operation(summary = "Dar de baja la propia cuenta (baja lógica: ya no podrá iniciar sesión)")
    public String dar_de_baja(@PathVariable Long idUsuario) {
        return service.dar_de_baja(idUsuario);
    }

    @PutMapping("{idUsuario}")
    @Operation(summary = "Editar el correo y el teléfono del perfil")
    public PerfilDto actualizar(@PathVariable Long idUsuario, @Valid @RequestBody PerfilDto perfil) {
        return service.actualizar_perfil(idUsuario, perfil);
    }
}
