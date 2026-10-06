package com.example.mediconnect_project.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private final long EXPIRATION_TIME = 86400000; // 24h

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // El token lleva el usuario, su rol y su perfil de paciente o médico (el que corresponda)
    public String generarToken(String username, String rol, Long idUsuario, Long idPaciente, Long idMedico) {
        JwtBuilder token = Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("id_usuario", idUsuario)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME));

        if (idPaciente != null) {
            token.claim("id_paciente", idPaciente);
        }
        if (idMedico != null) {
            token.claim("id_medico", idMedico);
        }

        return token.signWith(getSigningKey()).compact();
    }

    public boolean validarToken(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Lanza una excepción si el token es inválido, está alterado o venció
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
