package com.finanzas.auth.dominio.modelo;

import lombok.*;
import java.time.LocalDateTime;

/*
 * Este es el modelo de dominio del Usuario.
 * No tiene ninguna anotacion de JPA ni de Spring, eso va en la capa de infraestructura.
 * Aprendimos que en arquitectura hexagonal el dominio debe ser independiente de todo lo externo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    private Long id;
    private String correo;
    private String contrasena;  // siempre llega hasheada con BCrypt

    // Relacion con Cliente - cada usuario tiene un perfil de cliente
    private Long idCliente;

    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE_VERIFICACION;

    @Builder.Default
    private boolean correoVerificado = false;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Estados posibles de una cuenta
    public enum EstadoCuenta {
        PENDIENTE_VERIFICACION,
        ACTIVO,
        INACTIVO,
        BLOQUEADO
    }
}
