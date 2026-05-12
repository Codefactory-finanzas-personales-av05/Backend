package com.finanzas.auth.compartido;

import com.finanzas.auth.compartido.utilidad.GeneradorCodigo;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Pruebas del GeneradorCodigo.
 * No necesita Spring, es una clase utilitaria pura.
 */
@DisplayName("Pruebas del GeneradorCodigo")
class GeneradorCodigoTest {

    @Test
    @DisplayName("Codigo generado debe tener exactamente 6 digitos")
    void codigoGenerado_debeTenerSeisDigitos() {
        String codigo = GeneradorCodigo.generar(6);

        assertNotNull(codigo);
        assertEquals(6, codigo.length());
    }

    @Test
    @DisplayName("Codigo generado debe contener solo digitos")
    void codigoGenerado_debeTenerSoloDigitos() {
        String codigo = GeneradorCodigo.generar(6);

        assertTrue(codigo.matches("\\d+"),
                "El codigo debe contener solo numeros, pero fue: " + codigo);
    }

    @Test
    @DisplayName("Generar codigos de distintas longitudes funciona correctamente")
    void generarDistintasLongitudes_funcionaCorrectamente() {
        assertEquals(4, GeneradorCodigo.generar(4).length());
        assertEquals(6, GeneradorCodigo.generar(6).length());
        assertEquals(8, GeneradorCodigo.generar(8).length());
    }

    @Test
    @DisplayName("Generar multiples codigos produce resultados distintos")
    void generarMultiplesCodigos_producesResultadosDistintos() {
        // Generamos 50 codigos y verificamos que no todos son iguales
        Set<String> codigos = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            codigos.add(GeneradorCodigo.generar(6));
        }
        // Con 50 codigos de 6 digitos es practicamente imposible que todos sean iguales
        assertTrue(codigos.size() > 1,
                "Se esperaban codigos variados pero todos fueron iguales");
    }
}
