package com.example.mediconnect_project.Controller;

import com.example.mediconnect_project.Dto.LoginRespuestaDTO;
import com.example.mediconnect_project.Dto.RecuperarClaveDTO;
import com.example.mediconnect_project.Dto.RegistroMedicoDTO;
import com.example.mediconnect_project.Dto.RestablecerClaveDTO;
import com.example.mediconnect_project.Dto.RegistroPacienteDTO;
import com.example.mediconnect_project.Dto.ReenviarCodigoDTO;
import com.example.mediconnect_project.Dto.UsuarioDTO;
import com.example.mediconnect_project.Dto.VerificarCodigoDTO;
import com.example.mediconnect_project.Service.UsuarioService;
import com.example.mediconnect_project.Service.VerificacionService;
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

    @Autowired
    private VerificacionService verificacion;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con correo y contraseña. Con la verificación en dos pasos activa devuelve requiere_codigo e id_reto (sin token) y envía un código al correo; si no, devuelve el token JWT")
    public LoginRespuestaDTO login(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        LoginRespuestaDTO sesion = service.login(usuarioDTO);
        return verificacion.activo() ? verificacion.iniciar(sesion, usuarioDTO.getCorreo_usuario()) : sesion;
    }

    @PostMapping("/login/verificar")
    @Operation(summary = "Segundo paso del inicio de sesión: envía el código recibido por correo y devuelve el token JWT")
    public LoginRespuestaDTO verificar(@Valid @RequestBody VerificarCodigoDTO datos) {
        return verificacion.verificar(datos.getId_reto(), datos.getCodigo());
    }

    @PostMapping("/login/reenviar")
    @Operation(summary = "Envía un código nuevo al correo para un inicio de sesión pendiente")
    public LoginRespuestaDTO reenviar(@Valid @RequestBody ReenviarCodigoDTO datos) {
        return verificacion.reenviar(datos.getId_reto());
    }

    @PostMapping("/recuperar")
    @Operation(summary = "Recuperar contraseña, paso 1: envía un código al correo si la cuenta existe (la respuesta es la misma exista o no)")
    public LoginRespuestaDTO recuperar(@Valid @RequestBody RecuperarClaveDTO datos) {
        String correo = datos.getCorreo_usuario().trim();
        return verificacion.iniciarRecuperacion(correo, service.existe_cuenta_activa(correo));
    }

    @PostMapping("/recuperar/verificar")
    @Operation(summary = "Recuperar contraseña, paso 2: comprueba el código recibido por correo (sin gastarlo)")
    public String verificar_recuperacion(@Valid @RequestBody VerificarCodigoDTO datos) {
        verificacion.confirmarRecuperacion(datos.getId_reto(), datos.getCodigo(), false);
        return "Código correcto";
    }

    @PostMapping("/recuperar/restablecer")
    @Operation(summary = "Recuperar contraseña, paso 3: con el código recibido por correo define la nueva contraseña")
    public String restablecer(@Valid @RequestBody RestablecerClaveDTO datos) {
        String correo = verificacion.confirmarRecuperacion(datos.getId_reto(), datos.getCodigo(), true);
        return service.restablecer_clave(correo, datos.getClave_nueva());
    }
}
