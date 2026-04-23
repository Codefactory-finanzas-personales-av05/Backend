package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * DTO de respuesta al registrar un ingreso o gasto (HU-03 / HU-04).
 * Incluye el balance actualizado para que el frontend lo refleje de inmediato.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RespuestaTransaccion {

    private Long id;
    private String nombre;
    private BigDecimal monto;
    private LocalDateTime movimientoEn;
    private String tipo;
    private String nombreCategoria;
    private String iconoCategoria;

    // Balance actualizado tras registrar la transaccion (HU-03/HU-04)
    private BigDecimal balanceActual;

    private LocalDateTime creadoEn;
}
