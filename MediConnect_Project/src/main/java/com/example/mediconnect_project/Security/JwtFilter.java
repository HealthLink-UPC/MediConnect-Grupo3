package com.example.mediconnect_project.Security;

import com.example.mediconnect_project.Repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Exige el token JWT en todas las peticiones, salvo registro, login, la lista de roles y Swagger.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUt;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (es_publica(request)) {
            chain.doFilter(request, response);
            return;
        }

        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            rechazar(response, "Falta el token de autenticación");
            return;
        }

        Claims claims;
        try {
            claims = jwtUt.extraerClaims(cabecera.substring(7).trim());
        } catch (Exception e) {
            rechazar(response, "Token inválido o vencido");
            return;
        }

        // Una cuenta dada de baja deja de funcionar aunque su token no haya vencido
        Long idUsuario = numero(claims.get("id_usuario"));
        if (idUsuario == null || usuarioRepository.findById(idUsuario)
                .filter(u -> Integer.valueOf(1).equals(u.getEstado_usuario())).isEmpty()) {
            rechazar(response, "La cuenta no está activa");
            return;
        }

        try {
            UsuarioActual.establecer(
                    idUsuario,
                    claims.get("rol", String.class),
                    numero(claims.get("id_paciente")),
                    numero(claims.get("id_medico")));
            chain.doFilter(request, response);
        } finally {
            UsuarioActual.limpiar();
        }
    }

    private boolean es_publica(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        String metodo = request.getMethod();

        if (metodo.equals("OPTIONS")) {
            return true;
        }
        if (ruta.startsWith("/swagger-ui") || ruta.startsWith("/v3/api-docs") || ruta.equals("/error")) {
            return true;
        }
        return (metodo.equals("POST") && (ruta.equals("/auth/register/paciente") || ruta.equals("/auth/register/medico")
                || ruta.equals("/auth/login")))
                || (metodo.equals("GET") && ruta.equals("/roles/listar"));
    }

    private Long numero(Object valor) {
        return valor instanceof Number ? ((Number) valor).longValue() : null;
    }

    private void rechazar(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(("{\"error\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8));
    }
}
