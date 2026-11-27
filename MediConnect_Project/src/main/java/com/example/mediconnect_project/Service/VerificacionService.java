package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.LoginRespuestaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verificación en dos pasos al iniciar sesión: tras validar correo y contraseña se envía un código de 6 dígitos
 * al correo del usuario y solo con ese código se entrega el token.
 *
 * Los retos pendientes viven en memoria (no hay tabla): vencen a los N minutos, se usan una sola vez y se bloquean
 * tras varios intentos fallidos. Si se reinicia el servidor, quien esperaba su código debe iniciar sesión de nuevo.
 *
 * Si no hay correo configurado (propiedad spring.mail.username vacía) funciona en MODO PRUEBA: el código se escribe
 * en el log del servidor en lugar de enviarse.
 */
@Service
public class VerificacionService {

    private static final Logger log = LoggerFactory.getLogger(VerificacionService.class);
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Duration ESPERA_REENVIO = Duration.ofSeconds(30);

    @Value("${app.mfa.activo:true}")
    private boolean activo;

    @Value("${app.mfa.minutos:5}")
    private int minutosVigencia;

    @Value("${app.mfa.max-intentos:5}")
    private int maxIntentos;

    @Value("${spring.mail.username:}")
    private String remitente;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private static class Reto {
        final LoginRespuestaDTO sesion;
        final String correo;
        String codigo;
        Instant expira;
        Instant ultimoEnvio;
        int intentos;

        Reto(LoginRespuestaDTO sesion, String correo) {
            this.sesion = sesion;
            this.correo = correo;
        }
    }

    private final Map<String, Reto> retos = new ConcurrentHashMap<>();
    private final Map<String, Reto> recuperaciones = new ConcurrentHashMap<>();

    public boolean activo() {
        return activo;
    }

    /** Guarda la sesión ya validada, envía el código y devuelve la respuesta "falta el código" (sin token). */
    public LoginRespuestaDTO iniciar(LoginRespuestaDTO sesion, String correo) {
        purgar();
        Reto reto = new Reto(sesion, correo);
        String id = UUID.randomUUID().toString();
        generarYEnviar(reto);
        retos.put(id, reto);

        LoginRespuestaDTO pendiente = new LoginRespuestaDTO();
        pendiente.setRequiere_codigo(true);
        pendiente.setId_reto(id);
        pendiente.setCorreo_enmascarado(enmascarar(correo));
        return pendiente;
    }

    /** Valida el código; si es correcto devuelve la sesión con su token. */
    public LoginRespuestaDTO verificar(String idReto, String codigo) {
        purgar();
        Reto reto = buscar(idReto);

        if (reto.intentos >= maxIntentos) {
            retos.remove(idReto);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos. Inicia sesión nuevamente");
        }
        reto.intentos++;

        boolean ok = codigo != null && MessageDigest.isEqual(
                codigo.trim().getBytes(StandardCharsets.UTF_8), reto.codigo.getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El código es incorrecto");
        }

        retos.remove(idReto); // un código se usa una sola vez
        return reto.sesion;
    }

    /** Genera un código nuevo para el mismo reto (con una espera mínima entre envíos). */
    public LoginRespuestaDTO reenviar(String idReto) {
        purgar();
        Reto reto = buscar(idReto);
        if (reto.ultimoEnvio != null && Instant.now().isBefore(reto.ultimoEnvio.plus(ESPERA_REENVIO))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Espera unos segundos antes de pedir otro código");
        }
        reto.intentos = 0;
        generarYEnviar(reto);

        LoginRespuestaDTO pendiente = new LoginRespuestaDTO();
        pendiente.setRequiere_codigo(true);
        pendiente.setId_reto(idReto);
        pendiente.setCorreo_enmascarado(enmascarar(reto.correo));
        return pendiente;
    }

    /**
     * Recuperación de contraseña: envía un código al correo si la cuenta existe. La respuesta es igual exista o no
     * la cuenta, para no revelar qué correos están registrados.
     */
    public LoginRespuestaDTO iniciarRecuperacion(String correo, boolean existe) {
        purgar();
        Reto reto = new Reto(null, correo);
        String id = UUID.randomUUID().toString();
        if (existe) {
            generarYEnviar(reto, true);
        } else {
            reto.codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
            reto.expira = Instant.now().plus(Duration.ofMinutes(minutosVigencia));
        }
        recuperaciones.put(id, reto);

        LoginRespuestaDTO pendiente = new LoginRespuestaDTO();
        pendiente.setRequiere_codigo(true);
        pendiente.setId_reto(id);
        pendiente.setCorreo_enmascarado(enmascarar(correo));
        return pendiente;
    }

    /** Valida el código de recuperación y devuelve el correo al que pertenece (el código se usa una sola vez). */
    public String confirmarRecuperacion(String idReto, String codigo, boolean consumir) {
        purgar();
        Reto reto = idReto == null ? null : recuperaciones.get(idReto);
        if (reto == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El código venció. Solicita uno nuevo");
        }
        if (reto.intentos >= maxIntentos) {
            recuperaciones.remove(idReto);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos. Solicita un código nuevo");
        }
        reto.intentos++;
        boolean ok = codigo != null && MessageDigest.isEqual(
                codigo.trim().getBytes(StandardCharsets.UTF_8), reto.codigo.getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El código es incorrecto");
        }
        if (consumir) {
            recuperaciones.remove(idReto);
        } else {
            reto.intentos = 0; // el código es correcto: solo se comprueba, se gasta al cambiar la contraseña
        }
        return reto.correo;
    }

    private Reto buscar(String idReto) {
        Reto reto = idReto == null ? null : retos.get(idReto);
        if (reto == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El código venció. Inicia sesión nuevamente");
        }
        return reto;
    }

    private void generarYEnviar(Reto reto) {
        generarYEnviar(reto, false);
    }

    private void generarYEnviar(Reto reto, boolean recuperacion) {
        reto.codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
        reto.expira = Instant.now().plus(Duration.ofMinutes(minutosVigencia));
        reto.ultimoEnvio = Instant.now();
        enviar(reto, recuperacion);
    }

    private void enviar(Reto reto, boolean recuperacion) {
        boolean correoConfigurado = mailSender != null && remitente != null && !remitente.isBlank();
        if (!correoConfigurado) {
            log.warn("[MODO PRUEBA] No hay correo configurado. Código de verificación para {}: {}", reto.correo, reto.codigo);
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(reto.correo);
            mensaje.setSubject(recuperacion ? "MediConnect: recupera tu contraseña" : "MediConnect: tu código de verificación");
            mensaje.setText((recuperacion ? "Tu código para restablecer tu contraseña de MediConnect es: "
                    : "Tu código de verificación de MediConnect es: ") + reto.codigo
                    + "\n\nVence en " + minutosVigencia + " minutos. Si no fuiste tú, ignora este mensaje.");
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar el código de verificación a {}: {}", reto.correo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo enviar el código al correo. Inténtalo de nuevo");
        }
    }

    private void purgar() {
        Instant ahora = Instant.now();
        retos.values().removeIf(r -> r.expira != null && r.expira.isBefore(ahora));
        recuperaciones.values().removeIf(r -> r.expira != null && r.expira.isBefore(ahora));
    }

    /** lucia@gmail.com -> l***a@gmail.com */
    static String enmascarar(String correo) {
        if (correo == null || !correo.contains("@")) {
            return "tu correo";
        }
        int arroba = correo.indexOf('@');
        String usuario = correo.substring(0, arroba);
        String dominio = correo.substring(arroba);
        if (usuario.length() <= 2) {
            return usuario.charAt(0) + "***" + dominio;
        }
        return usuario.charAt(0) + "***" + usuario.charAt(usuario.length() - 1) + dominio;
    }
}
