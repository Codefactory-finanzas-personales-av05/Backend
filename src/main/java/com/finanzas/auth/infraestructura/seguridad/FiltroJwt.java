package com.finanzas.auth.infraestructura.seguridad;

import com.finanzas.auth.compartido.utilidad.UtilJwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/*
 * I5 CORREGIDO: Filtro JWT que se ejecuta en cada request.
 *
 * Antes: el token se generaba en el login pero NUNCA se validaba.
 * Ahora: este filtro intercepta cada request, extrae el token del header
 * Authorization y lo valida antes de que llegue al controlador.
 *
 * Formato esperado del header: Authorization: Bearer eyJhbGci...
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FiltroJwt extends OncePerRequestFilter {

    private final UtilJwt utilJwt;

    @Override
    protected void doFilterInternal(


            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        

        String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con Bearer, dejamos pasar sin autenticar
        // Spring Security rechazara si el endpoint lo requiere
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (utilJwt.esTokenValido(token)) {
                String correo = utilJwt.extraerCorreo(token);

                if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    correo, null, new ArrayList<>());

                    autenticacion.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    log.debug("Usuario autenticado via JWT: {}", correo);
                }
            }
        } catch (Exception ex) {
            log.warn("Token JWT invalido en {}: {}", request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
