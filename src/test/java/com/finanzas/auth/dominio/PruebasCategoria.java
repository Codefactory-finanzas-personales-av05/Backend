package com.finanzas.auth.dominio;

import com.finanzas.auth.dominio.modelo.Categoria;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias del modelo Categoria")
class PruebasCategoria {

    @Test
    @DisplayName("Categoria de ingreso tiene tipo INGRESO")
    void categoriaIngreso_tienetipoIngreso() {
        Categoria categoria = Categoria.builder()
                .idCategoria(1L)
                .nombre("Salario")
                .icono("💼")
                .tipo(Categoria.TipoCategoria.INGRESO)
                .idCliente(1L)
                .build();

        assertEquals(Categoria.TipoCategoria.INGRESO, categoria.getTipo());
        assertEquals("Salario", categoria.getNombre());
        assertEquals("💼", categoria.getIcono());
        assertEquals(1L, categoria.getIdCliente());
    }

    @Test
    @DisplayName("Categoria de gasto tiene tipo GASTO")
    void categoriaGasto_tieneTipoGasto() {
        Categoria categoria = Categoria.builder()
                .idCategoria(2L)
                .nombre("Comida")
                .icono("🍔")
                .tipo(Categoria.TipoCategoria.GASTO)
                .idCliente(1L)
                .build();

        assertEquals(Categoria.TipoCategoria.GASTO, categoria.getTipo());
    }

    @Test
    @DisplayName("El enum TipoCategoria tiene exactamente INGRESO y GASTO")
    void tipoCategoria_tieneValoresCorrectos() {
        Categoria.TipoCategoria[] valores = Categoria.TipoCategoria.values();
        assertEquals(2, valores.length);
        assertEquals(Categoria.TipoCategoria.INGRESO, Categoria.TipoCategoria.valueOf("INGRESO"));
        assertEquals(Categoria.TipoCategoria.GASTO, Categoria.TipoCategoria.valueOf("GASTO"));
    }
}
