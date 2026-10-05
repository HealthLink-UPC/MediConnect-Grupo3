package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.LoginRespuestaDTO;
import com.example.mediconnect_project.Dto.RegistroMedicoDTO;
import com.example.mediconnect_project.Dto.RegistroPacienteDTO;
import com.example.mediconnect_project.Dto.UsuarioDTO;
import com.example.mediconnect_project.Service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Controlador de autenticación", description = "Permite registrar pacientes y médicos por separado e iniciar sesión")
public class AuthController {

    @Autowired
    private UsuarioService service;

    @PostMapping("/register/paciente")
    @Operation(summary = "Registrar un paciente (datos personales; los datos clínicos se completan después en su ficha)")
    public String register_paciente(@Valid @RequestBody RegistroPacienteDTO registroDTO) {
        return service.registrar_paciente(registroDTO);
    }

    @PostMapping("/register/medico")
    @Operation(summary = "Registrar un médico (datos personales y profesionales)")
    public String register_medico(@Valid @RequestBody RegistroMedicoDTO registroDTO) {
        return service.registrar_medico(registroDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con correo y contraseña; devuelve el token JWT")
    public LoginRespuestaDTO login(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        return service.login(usuarioDTO);
    }
}
