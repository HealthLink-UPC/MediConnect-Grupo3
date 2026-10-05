package com.example.mediconnect_project.Service.Impl;

import com.example.mediconnect_project.Dto.LoginRespuestaDTO;
import com.example.mediconnect_project.Dto.PerfilDto;
import com.example.mediconnect_project.Dto.DatosRegistro;
import com.example.mediconnect_project.Dto.RegistroMedicoDTO;
import com.example.mediconnect_project.Dto.RegistroPacienteDTO;
import com.example.mediconnect_project.Dto.UsuarioDTO;
import com.example.mediconnect_project.Entity.MedicoEntity;
import com.example.mediconnect_project.Entity.PacienteEntity;
import com.example.mediconnect_project.Entity.RolEntity;
import com.example.mediconnect_project.Entity.UsuarioEntity;
import com.example.mediconnect_project.Repository.MedicoRepository;
import com.example.mediconnect_project.Repository.PacienteRepository;
import com.example.mediconnect_project.Repository.RolRepository;
import com.example.mediconnect_project.Repository.UsuarioRepository;
import com.example.mediconnect_project.Security.JwtUtil;
import com.example.mediconnect_project.Security.UsuarioActual;
import com.example.mediconnect_project.Service.AuditoriaService;
import com.example.mediconnect_project.Service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    public static final int ESTADO_ACTIVO = 1;
    public static final int ESTADO_INACTIVO = 0;

    public static final String ROL_PACIENTE = "PACIENTE";
    public static final String ROL_MEDICO = "MEDICO";

    // tm_paciente exige estos campos; hasta que el paciente complete su ficha se guardan estos valores
    public static final String SANGRE_SIN_REGISTRAR = "SIN REGISTRAR";
    public static final double MEDIDA_SIN_REGISTRAR = 0.0;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private JwtUtil jwtUt;

    @Autowired
    private AuditoriaService auditoriaService;

    @Override
    @Transactional
    public String registrar_paciente(RegistroPacienteDTO registro) {
        UsuarioEntity usuario = crear_usuario(registro, ROL_PACIENTE);

        PacienteEntity paciente = new PacienteEntity();
        paciente.setTipo_sangre(SANGRE_SIN_REGISTRAR);
        paciente.setPeso(MEDIDA_SIN_REGISTRAR);
        paciente.setTalla(MEDIDA_SIN_REGISTRAR);
        paciente.setEstado_paciente(ESTADO_ACTIVO);
        paciente.setUsuario(usuario);
        paciente = pacienteRepository.save(paciente);
        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_PACIENTE, paciente.getId_paciente(), usuario.getId_usuario());

        log.info("Paciente registrado");
        return "Paciente registrado con éxito";
    }

    @Override
    @Transactional
    public String registrar_medico(RegistroMedicoDTO registro) {
        if (medicoRepository.existe_cmp(registro.getCmp())) {
            throw new IllegalArgumentException("El CMP ya se encuentra registrado");
        }

        UsuarioEntity usuario = crear_usuario(registro, ROL_MEDICO);

        MedicoEntity medico = new MedicoEntity();
        medico.setEspecialidad(registro.getEspecialidad());
        medico.setCmp(registro.getCmp());
        medico.setCentro_atencion(registro.getCentro_atencion());
        medico.setEstado_medico(ESTADO_ACTIVO);
        medico.setUsuario(usuario);
        medico = medicoRepository.save(medico);
        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_MEDICO, medico.getId_medico(), usuario.getId_usuario());

        log.info("Médico registrado");
        return "Médico registrado con éxito";
    }

    // Datos comunes del registro: valida correo y DNI, crea el usuario con su rol y deja la auditoría
    // (quien se registra es quien crea su propia cuenta: id de usuario y id de registro son el mismo)
    private UsuarioEntity crear_usuario(DatosRegistro registro, String nombreRol) {
        RolEntity rol = rolRepository.buscar_por_nombre(nombreRol)
                .filter(r -> Integer.valueOf(ESTADO_ACTIVO).equals(r.getEstado_rol()))
                .orElseThrow(() -> new IllegalStateException("El rol " + nombreRol + " no está configurado"));

        if (repository.existe_correo(registro.getCorreo_usuario())) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }
        if (repository.existe_dni(registro.getDni())) {
            throw new IllegalArgumentException("El DNI ya se encuentra registrado");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setRol(rol);
        usuario.setCorreo_usuario(registro.getCorreo_usuario());
        usuario.setClave_usuario(new BCryptPasswordEncoder().encode(registro.getClave_usuario()));
        usuario.setNombre_usuario(registro.getNombre_usuario());
        usuario.setApellido_usuario(registro.getApellido_usuario());
        usuario.setDni(registro.getDni());
        usuario.setFecha_nacimiento(registro.getFecha_nacimiento());
        usuario.setTelefono(registro.getTelefono());
        usuario.setEstado_usuario(ESTADO_ACTIVO);
        usuario = repository.save(usuario);

        auditoriaService.registrar_alta(AuditoriaServiceImpl.TABLA_USUARIO, usuario.getId_usuario(), usuario.getId_usuario());
        return usuario;
    }

    @Override
    public LoginRespuestaDTO login(UsuarioDTO usuarioDTO) {
        UsuarioEntity user = repository.buscar_por_correo(usuarioDTO.getCorreo_usuario());

        if (user == null
                || !Integer.valueOf(ESTADO_ACTIVO).equals(user.getEstado_usuario())
                || !new BCryptPasswordEncoder().matches(usuarioDTO.getClave_usuario(), user.getClave_usuario())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String nombreRol = user.getRol().getNombre_rol();

        LoginRespuestaDTO respuesta = new LoginRespuestaDTO();
        respuesta.setId_usuario(user.getId_usuario());
        respuesta.setRol_usuario(nombreRol);
        respuesta.setNombre_usuario(user.getNombre_usuario() + " " + user.getApellido_usuario());

        if (ROL_MEDICO.equals(nombreRol)) {
            medicoRepository.buscar_por_usuario(user.getId_usuario())
                    .ifPresent(medico -> respuesta.setId_medico(medico.getId_medico()));
        } else {
            pacienteRepository.buscar_por_usuario(user.getId_usuario())
                    .ifPresent(paciente -> respuesta.setId_paciente(paciente.getId_paciente()));
        }

        // El token lleva el usuario, su rol y su perfil de paciente o médico
        respuesta.setToken(jwtUt.generarToken(user.getCorreo_usuario(), nombreRol, user.getId_usuario(),
                respuesta.getId_paciente(), respuesta.getId_medico()));

        return respuesta;
    }

    @Override
    public PerfilDto obtener_perfil(Long idUsuario) {
        UsuarioActual.exigir_usuario(idUsuario);
        return convertir(buscar_usuario(idUsuario));
    }

    @Override
    @Transactional
    public PerfilDto actualizar_perfil(Long idUsuario, PerfilDto perfil) {
        UsuarioActual.exigir_usuario(idUsuario);
        UsuarioEntity usuario = buscar_usuario(idUsuario);

        if (repository.existe_correo_de_otro(perfil.getCorreo_usuario(), idUsuario)) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }

        usuario.setCorreo_usuario(perfil.getCorreo_usuario());
        usuario.setTelefono(perfil.getTelefono());

        usuario = repository.save(usuario);
        auditoriaService.registrar_edicion(AuditoriaServiceImpl.TABLA_USUARIO, idUsuario, idUsuario);

        return convertir(usuario);
    }

    @Override
    @Transactional
    public String dar_de_baja(Long idUsuario) {
        UsuarioActual.exigir_usuario(idUsuario);
        UsuarioEntity usuario = buscar_usuario(idUsuario);

        if (!Integer.valueOf(ESTADO_ACTIVO).equals(usuario.getEstado_usuario())) {
            throw new IllegalArgumentException("La cuenta ya está dada de baja");
        }

        usuario.setEstado_usuario(ESTADO_INACTIVO);
        repository.save(usuario);
        auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_USUARIO, idUsuario, idUsuario);

        // También queda inactivo su perfil de médico o de paciente
        medicoRepository.buscar_por_usuario(idUsuario).ifPresent(medico -> {
            medico.setEstado_medico(ESTADO_INACTIVO);
            medicoRepository.save(medico);
            auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_MEDICO, medico.getId_medico(), idUsuario);
        });
        pacienteRepository.buscar_por_usuario(idUsuario).ifPresent(paciente -> {
            paciente.setEstado_paciente(ESTADO_INACTIVO);
            pacienteRepository.save(paciente);
            auditoriaService.registrar_eliminacion(AuditoriaServiceImpl.TABLA_PACIENTE, paciente.getId_paciente(), idUsuario);
        });

        log.info("Cuenta del usuario {} dada de baja", idUsuario);
        return "Cuenta dada de baja";
    }

    private UsuarioEntity buscar_usuario(Long idUsuario) {
        return repository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private PerfilDto convertir(UsuarioEntity usuario) {
        PerfilDto perfil = new PerfilDto();
        perfil.setId_usuario(usuario.getId_usuario());
        perfil.setRol_usuario(usuario.getRol().getNombre_rol());
        perfil.setNombre_usuario(usuario.getNombre_usuario());
        perfil.setApellido_usuario(usuario.getApellido_usuario());
        perfil.setDni(usuario.getDni());
        perfil.setFecha_nacimiento(usuario.getFecha_nacimiento());
        perfil.setCorreo_usuario(usuario.getCorreo_usuario());
        perfil.setTelefono(usuario.getTelefono());
        return perfil;
    }
}
