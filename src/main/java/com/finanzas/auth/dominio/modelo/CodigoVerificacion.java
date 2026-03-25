package com.finanzas.auth.dominio.modelo;

import lombok.*;
import java.time.LocalDateTime;

/*
 * Modelo de dominio para los codigos de verificacion que se envian al correo.
 * Tambien sin anotaciones de JPA, igual que Usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoVerificacion {

    private Long id;
    private Long usuarioId;
    private String codigo;
    private TipoCodigo tipo;
    private LocalDateTime fechaExpiracion;

    @Builder.Default
    private boolean usado = false;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /*
     * Regla de negocio: un codigo es valido si no fue usado
     * y todavia no ha expirado.
     */
    public boolean estaVigente() {
        return !usado && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public enum TipoCodigo {
        VERIFICACION_CORREO,
        RECUPERACION_CONTRASENA,
        DOS_FACTORES
    }
}
