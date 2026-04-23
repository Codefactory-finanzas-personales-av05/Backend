package com.finanzas.auth.dominio.modelo;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Modelo de dominio de Transaccion.
 * Representa un ingreso o gasto registrado por un cliente.
 * Sin anotaciones JPA — el dominio no sabe de bases de datos.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaccion {

    private Long id;
    private String nombre;
    private BigDecimal monto;
    private LocalDateTime movimientoEn;
    private TipoTransaccion tipo;
    private Long idCliente;
    private Long idCategoria;

    // Campos denormalizados para no hacer join extra al devolver el historial
    private String nombreCategoria;
    private String iconoCategoria;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public enum TipoTransaccion {
        INGRESO,
        GASTO
    }
}
