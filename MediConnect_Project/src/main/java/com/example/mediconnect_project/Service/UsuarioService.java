package com.example.mediconnect_project.Service;

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

    // El usuario da de baja su propia cuenta (baja lógica: estado 0); ya no puede iniciar sesión
    String dar_de_baja(Long idUsuario);
}
