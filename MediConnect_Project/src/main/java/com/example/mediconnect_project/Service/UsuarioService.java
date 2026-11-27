package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.CambioClaveDTO;
import com.example.mediconnect_project.Dto.LoginRespuestaDTO;
import com.example.mediconnect_project.Dto.PerfilDto;
import com.example.mediconnect_project.Dto.RegistroMedicoDTO;
import com.example.mediconnect_project.Dto.RegistroPacienteDTO;
import com.example.mediconnect_project.Dto.UsuarioDTO;

public interface UsuarioService {

    String registrar_paciente(RegistroPacienteDTO registro);

    String registrar_medico(RegistroMedicoDTO registro);

    LoginRespuestaDTO login(UsuarioDTO usuarioDTO);

    PerfilDto obtener_perfil(Long idUsuario);

    PerfilDto actualizar_perfil(Long idUsuario, PerfilDto perfil);

    // El usuario cambia su propia contraseña confirmando la actual
    String cambiar_clave(Long idUsuario, CambioClaveDTO cambio);

    // Recuperación de contraseña (sin sesión): la cuenta existe y está activa / se define una nueva clave
    boolean existe_cuenta_activa(String correo);

    String restablecer_clave(String correo, String claveNueva);

    // El usuario da de baja su propia cuenta (baja lógica: estado 0); ya no puede iniciar sesión
    String dar_de_baja(Long idUsuario);
}
