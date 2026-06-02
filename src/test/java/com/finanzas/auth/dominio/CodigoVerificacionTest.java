package com.finanzas.auth.dominio;

import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Pruebas unitarias del modelo de dominio CodigoVerificacion.
 * Se prueba la regla de negocio estaVigente() que es la mas importante
 * de este modelo. No necesita Spring ni base de datos.
 */
@DisplayName("Pruebas del modelo CodigoVerificacion")
class CodigoVerificacionTest {

    @Test
    @DisplayName("Codigo no usado y no expirado debe estar vigente")
    void codigoNoUsadoNoExpirado_debeEstarVigente() {
        CodigoVerificacion codigo = CodigoVerificacion.builder()
                .codigo("123456")
                .usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();

        assertTrue(codigo.estaVigente());
    }

    @Test
    @DisplayName("Codigo ya usado no debe estar vigente")
    void codigoUsado_noDebeEstarVigente() {
        CodigoVerificacion codigo = CodigoVerificacion.builder()
                .codigo("123456")
                .usado(true)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();

        assertFalse(codigo.estaVigente());
    }

    @Test
    @DisplayName("Codigo expirado no debe estar vigente")
    void codigoExpirado_noDebeEstarVigente() {
        CodigoVerificacion codigo = CodigoVerificacion.builder()
                .codigo("123456")
                .usado(false)
                .fechaExpiracion(LocalDateTime.now().minusMinutes(5))
                .build();

        assertFalse(codigo.estaVigente());
    }

    @Test
    @DisplayName("Codigo usado y expirado no debe estar vigente")
    void codigoUsadoYExpirado_noDebeEstarVigente() {
        CodigoVerificacion codigo = CodigoVerificacion.builder()
                .codigo("123456")
                .usado(true)
                .fechaExpiracion(LocalDateTime.now().minusMinutes(20))
                .build();

        assertFalse(codigo.estaVigente());
    }

    @Test
    @DisplayName("Codigo con expiracion en exactamente ahora no debe estar vigente")
    void codigoExpiradoAhora_noDebeEstarVigente() {
        CodigoVerificacion codigo = CodigoVerificacion.builder()
                .codigo("123456")
                .usado(false)
                .fechaExpiracion(LocalDateTime.now().minusNanos(1))
                .build();

        assertFalse(codigo.estaVigente());
    }

    @Test
    @DisplayName("Codigo builder debe tener usado=false por defecto")
    void codigoBuilder_debeTenerUsadoFalseporDefecto() {
        CodigoVerificacion codigo = CodigoVerificacion.builder()
                .codigo("654321")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
                .build();

        assertFalse(codigo.isUsado());
        assertNotNull(codigo.getFechaCreacion());
    }

    @Test
    @DisplayName("Tipos de codigo deben tener los valores correctos")
    void tiposCodigo_debenTenerValoresCorrectos() {
        assertEquals(3, CodigoVerificacion.TipoCodigo.values().length);
        assertNotNull(CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO);
        assertNotNull(CodigoVerificacion.TipoCodigo.RECUPERACION_CONTRASENA);
        assertNotNull(CodigoVerificacion.TipoCodigo.DOS_FACTORES);
    }
}
