package com.finanzas.auth.dominio;

import com.finanzas.auth.dominio.modelo.Transaccion;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias del modelo Transaccion")
class TransaccionTest {

    @Test
    @DisplayName("Transaccion de ingreso se construye correctamente")
    void transaccionIngreso_seConstruyeCorrectamente() {
        BigDecimal monto = new BigDecimal("3000000.00");
        LocalDateTime fecha = LocalDateTime.of(2026, 4, 23, 8, 0);

        Transaccion transaccion = Transaccion.builder()
                .id(1L)
                .nombre("Salario abril")
                .monto(monto)
                .movimientoEn(fecha)
                .tipo(Transaccion.TipoTransaccion.INGRESO)
                .idCliente(1L)
                .idCategoria(10L)
                .build();

        assertEquals(1L, transaccion.getId());
        assertEquals("Salario abril", transaccion.getNombre());
        assertEquals(monto, transaccion.getMonto());
        assertEquals(fecha, transaccion.getMovimientoEn());
        assertEquals(Transaccion.TipoTransaccion.INGRESO, transaccion.getTipo());
        assertEquals(1L, transaccion.getIdCliente());
    }

    @Test
    @DisplayName("Transaccion de gasto tiene tipo GASTO")
    void transaccionGasto_tieneTipoGasto() {
        Transaccion transaccion = Transaccion.builder()
                .nombre("Almuerzo")
                .monto(new BigDecimal("15000"))
                .movimientoEn(LocalDateTime.now())
                .tipo(Transaccion.TipoTransaccion.GASTO)
                .idCliente(1L)
                .idCategoria(20L)
                .build();

        assertEquals(Transaccion.TipoTransaccion.GASTO, transaccion.getTipo());
    }

    @Test
    @DisplayName("El enum TipoTransaccion tiene exactamente INGRESO y GASTO")
    void tipoTransaccion_tieneValoresCorrectos() {
        Transaccion.TipoTransaccion[] valores = Transaccion.TipoTransaccion.values();
        assertEquals(2, valores.length);
        assertEquals(Transaccion.TipoTransaccion.INGRESO, Transaccion.TipoTransaccion.valueOf("INGRESO"));
        assertEquals(Transaccion.TipoTransaccion.GASTO, Transaccion.TipoTransaccion.valueOf("GASTO"));
    }

    @Test
    @DisplayName("Monto de la transaccion mantiene precision de 2 decimales")
    void monto_mantienePrecision() {
        BigDecimal monto = new BigDecimal("15000.50");
        Transaccion t = Transaccion.builder()
                .nombre("Pago parcial")
                .monto(monto)
                .movimientoEn(LocalDateTime.now())
                .tipo(Transaccion.TipoTransaccion.GASTO)
                .idCliente(1L).idCategoria(1L).build();

        assertEquals(0, monto.compareTo(t.getMonto()));
    }
}
