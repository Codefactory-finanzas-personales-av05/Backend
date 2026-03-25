package com.finanzas.auth.compartido.utilidad;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/*
 * Utilidad para generar y validar tokens JWT.
 * El token lleva el correo y el ID del usuario para identificarlo en cada request.
 */
@Component
@Slf4j
public class UtilJwt {

    @Value("${app.jwt.secret}")
    private String secreto;

    @Value("${app.jwt.expiration-ms}")
    private long expiracionMs;

    // Genera un nuevo token para el usuario que acaba de hacer login
    public String generarToken(String correo, Long usuarioId) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .setSubject(correo)
                .claim("usuarioId", usuarioId)
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(obtenerClave(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrae el correo del token (se usa para identificar al usuario)
    public String extraerCorreo(String token) {
        return parsearToken(token).getBody().getSubject();
    }

    // Verifica que el token sea valido y no haya expirado
    public boolean esTokenValido(String token) {
        try {
            parsearToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Token JWT invalido: {}", ex.getMessage());
            return false;
        }
    }

    private Jws<Claims> parsearToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(obtenerClave())
                .build()
                .parseClaimsJws(token);
    }

    private Key obtenerClave() {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }
}
