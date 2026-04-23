package com.finanzas.auth.compartido;

import com.finanzas.auth.compartido.utilidad.UtilJwt;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Pruebas del UtilJwt")
class PruebasUtilJwt {

    @Autowired
    private UtilJwt utilJwt;

    @Test
    @DisplayName("Token generado no debe ser nulo ni vacio")
    void generarToken_noDebeSerNuloNiVacio() {
        String token = utilJwt.generarToken("usuario@test.com", 1L);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Token generado debe ser valido")
    void tokenGenerado_debeSerValido() {
        String token = utilJwt.generarToken("usuario@test.com", 1L);
        assertTrue(utilJwt.esTokenValido(token));
    }

    @Test
    @DisplayName("Token alterado no debe ser valido")
    void tokenAlterado_noDebeSerValido() {
        assertFalse(utilJwt.esTokenValido("eyJhbGciOiJIUzI1NiJ9.tokenFalso.firmaInvalida"));
    }

    @Test
    @DisplayName("String vacio no debe ser token valido")
    void stringVacio_noDebeSerTokenValido() {
        assertFalse(utilJwt.esTokenValido(""));
    }

    @Test
    @DisplayName("Extraer correo debe devolver el correo original")
    void extraerCorreo_devolverCorreoOriginal() {
        String correoOriginal = "usuario@test.com";
        String token = utilJwt.generarToken(correoOriginal, 1L);
        assertEquals(correoOriginal, utilJwt.extraerCorreo(token));
    }

    @Test
    @DisplayName("Tokens de usuarios distintos deben ser distintos")
    void tokensDistintos_paraUsuariosDistintos() {
        String token1 = utilJwt.generarToken("usuario1@test.com", 1L);
        String token2 = utilJwt.generarToken("usuario2@test.com", 2L);
        assertNotEquals(token1, token2);
    }
}
