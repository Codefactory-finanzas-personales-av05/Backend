package com.finanzas.auth.dominio.modelo;

import lombok.*;
import java.time.LocalDateTime;

/*
 * Modelo de dominio del Usuario.
 * Sin anotaciones de JPA ni de Spring.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    private Long id;
    private String correo;
    private String contrasena;
    private Long idCliente;

    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE_VERIFICACION;

    @Builder.Default
    private boolean correoVerificado = false;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public enum EstadoCuenta {
        PENDIENTE_VERIFICACION,
        ACTIVO,
        INACTIVO,
        BLOQUEADO
    }
}
