package com.finanzas.auth.dominio.modelo;

import lombok.*;
import java.time.LocalDateTime;

/*
 * Este es el modelo de dominio del Usuario.
 * 
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
