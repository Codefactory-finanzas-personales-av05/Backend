package com.finanzas.auth.dominio.puertos.salida;

import com.finanzas.auth.dominio.modelo.Transaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/*
 * Puerto de SALIDA para transacciones.
 */
public interface PuertoRepositorioTransaccion {

    Transaccion guardar(Transaccion transaccion);

    // Historial paginado ordenado por movimiento_en DESC (HU-05)
    Page<Transaccion> buscarPorIdCliente(Long idCliente, Pageable pageable);

    // Suma de todos los montos por tipo para calcular el balance (HU-03/HU-04)
    BigDecimal sumarMontosPorTipo(Long idCliente, Transaccion.TipoTransaccion tipo);
}
