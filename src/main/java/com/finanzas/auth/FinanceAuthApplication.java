package com.finanzas.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Punto de entrada de la aplicacion
@SpringBootApplication
@EnableJpaAuditing
public class FinanceAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceAuthApplication.class, args);
    }
}
