package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/*
 * Respuesta del historial paginado de transacciones (HU-05).
 * Incluye el balance general para mostrar el resumen financiero.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RespuestaHistorial {

    // Lista de transacciones de la pagina actual
    private List<RespuestaItemHistorial> transacciones;

    // Datos de paginacion
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;

    // Balance financiero del cliente
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal balanceActual;   // totalIngresos - totalGastos
}
