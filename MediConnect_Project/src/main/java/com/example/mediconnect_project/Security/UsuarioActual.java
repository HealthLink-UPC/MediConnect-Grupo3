package com.example.mediconnect_project.Security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Usuario que hace la petición actual, tomado del token por JwtFilter.
 * Los servicios lo usan para validar el rol y que cada quien solo toque lo suyo.
 */
public final class UsuarioActual {

    public static final String ROL_PACIENTE = "PACIENTE";
    public static final String ROL_MEDICO = "MEDICO";

    private static final String MENSAJE_PROHIBIDO = "No tiene permiso para realizar esta acción";

    private static final ThreadLocal<Datos> ACTUAL = new ThreadLocal<>();

    public record Datos(Long id_usuario, String rol, Long id_paciente, Long id_medico) {
    }

    private UsuarioActual() {
    }

    public static void establecer(Long idUsuario, String rol, Long idPaciente, Long idMedico) {
        ACTUAL.set(new Datos(idUsuario, rol, idPaciente, idMedico));
    }

    public static void limpiar() {
        ACTUAL.remove();
    }

    private static Datos datos() {
        Datos datos = ACTUAL.get();
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta el token de autenticación");
        }
        return datos;
    }

    public static Long id_usuario() {
        return datos().id_usuario();
    }

    public static String rol() {
        return datos().rol();
    }

    // Id del paciente que hace la petición; solo existe si el rol es PACIENTE
    public static Long id_paciente() {
        Datos datos = datos();
        if (!ROL_PACIENTE.equals(datos.rol()) || datos.id_paciente() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MENSAJE_PROHIBIDO);
        }
        return datos.id_paciente();
    }

    public static void exigir_usuario(Long idUsuario) {
        if (!datos().id_usuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MENSAJE_PROHIBIDO);
        }
    }

    public static void exigir_paciente(Long idPaciente) {
        if (!id_paciente().equals(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MENSAJE_PROHIBIDO);
        }
    }

    public static void exigir_medico(Long idMedico) {
        Datos datos = datos();
        if (!ROL_MEDICO.equals(datos.rol()) || datos.id_medico() == null || !datos.id_medico().equals(idMedico)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MENSAJE_PROHIBIDO);
        }
    }

    // El paciente solo accede a lo suyo; cualquier médico puede consultar (por ejemplo, la ficha antes de aprobar)
    public static void exigir_paciente_o_medico(Long idPaciente) {
        Datos datos = datos();
        if (ROL_MEDICO.equals(datos.rol())) {
            return;
        }
        exigir_paciente(idPaciente);
    }
}
