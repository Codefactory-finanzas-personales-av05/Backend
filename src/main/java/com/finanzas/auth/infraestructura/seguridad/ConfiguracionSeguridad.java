package com.finanzas.auth.infraestructura.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Configuracion de seguridad de Spring Security.
 * Los endpoints de autenticacion son publicos, el resto requiere token.
 * La H2 Console tambien se abre para poder ver los datos en pruebas.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    // Endpoints que no necesitan token para acceder
    private static final String[] ENDPOINTS_PUBLICOS = {
        "/api/auth/**",
        "/h2-console/**",
        "/actuator/health"
    };

    @Bean
    public SecurityFilterChain configurarSeguridad(HttpSecurity http) throws Exception {
        http
            // Desactivamos CSRF porque usamos JWT (sin sesion)
            .csrf(AbstractHttpConfigurer::disable)

            // Sin sesion - cada request trae su propio token
            .sessionManagement(sesion ->
                sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Reglas de acceso
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ENDPOINTS_PUBLICOS).permitAll()
                .anyRequest().authenticated()
            )

            // Necesario para que el navegador pueda cargar los frames de H2 Console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorContrasena() {
        // BCrypt con factor de costo 12 (buen balance entre seguridad y velocidad)
        return new BCryptPasswordEncoder(12);
    }
}
