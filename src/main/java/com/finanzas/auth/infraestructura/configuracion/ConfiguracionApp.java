package com.finanzas.auth.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * Configuracion general de la aplicacion.
 * Por ahora solo configura CORS para que el frontend en React
 * pueda hacer peticiones sin que el navegador las bloquee.
 */
@Configuration
public class ConfiguracionApp {

    @Bean
    public WebMvcConfigurer configurarCors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registro) {
                registro.addMapping("/api/**")
                        // Aqui van los origenes permitidos (el frontend)
                        .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
