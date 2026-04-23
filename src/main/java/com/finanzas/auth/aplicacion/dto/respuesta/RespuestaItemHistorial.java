package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Un item del historial de transacciones (HU-05).
 * Contiene los campos que se muestran en cada fila del listado.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RespuestaItemHistorial {

    private Long id;
    private String nombre;
    private BigDecimal monto;
    private LocalDateTime movimientoEn;
    private String tipo;             // "INGRESO" o "GASTO"
    private String nombreCategoria;
    private String iconoCategoria;
}
