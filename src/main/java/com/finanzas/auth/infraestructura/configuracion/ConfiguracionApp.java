package com.finanzas.auth.infraestructura.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * Configuracion general de la aplicacion.
 *
 * IMPORTANTE: Este archivo debe estar en la carpeta "configuracion",
 * no en "correo". El paquete es com.finanzas.auth.infraestructura.configuracion
 *
 * I8 CORREGIDO: RestTemplate es un Bean de Spring — inyectable y mockeable en tests.
 * M10 CORREGIDO: CORS lee los origenes desde variable de entorno CORS_ORIGINS.
 */
@Configuration
public class ConfiguracionApp {

    @Value("${app.cors.origenes-permitidos}")
    private String origenesPermitidos;

    // I8: RestTemplate como Bean en vez de "new RestTemplate()" en cada clase
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebMvcConfigurer configurarCors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registro) {
                // Soporta multiples origenes separados por coma
                String[] origenes = origenesPermitidos.split(",");

                registro.addMapping("/api/**")
                        .allowedOrigins(origenes)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                // Swagger UI
                registro.addMapping("/swagger-ui/**")
                        .allowedOrigins(origenes)
                        .allowedMethods("GET");

                registro.addMapping("/v3/api-docs/**")
                        .allowedOrigins(origenes)
                        .allowedMethods("GET");
            }
        };
    }
}
