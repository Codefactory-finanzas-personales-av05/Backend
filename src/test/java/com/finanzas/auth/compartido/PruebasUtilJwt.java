package com.finanzas.auth.compartido;

import com.finanzas.auth.compartido.utilidad.UtilJwt;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Pruebas del UtilJwt.
 * Usamos @SpringBootTest para que Spring inyecte el secreto desde application.yml.
 */
@SpringBootTest
@DisplayName("Pruebas del UtilJwt")
class PruebasUtilJwt {

    @Autowired
    private UtilJwt utilJwt;

    @Test
    @DisplayName("Generar token no debe retornar nulo ni vacio")
    void generarToken_noDebeSerNuloNiVacio() {
        String token = utilJwt.generarToken("usuario@test.com", 1L);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // Un JWT siempre tiene 3 partes separadas por puntos
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
        String tokenAlterado = "eyJhbGciOiJIUzI1NiJ9.tokenFalso.firmaInvalida";

        assertFalse(utilJwt.esTokenValido(tokenAlterado));
    }

    @Test
    @DisplayName("String vacio no debe ser un token valido")
    void stringVacio_noDebeSerTokenValido() {
        assertFalse(utilJwt.esTokenValido(""));
    }

    @Test
    @DisplayName("Extraer correo del token debe devolver el correo original")
    void extraerCorreo_debeDevolver elCorreoOriginal() {
        String correoOriginal = "usuario@test.com";
        String token = utilJwt.generarToken(correoOriginal, 1L);

        String correoExtraido = utilJwt.extraerCorreo(token);

        assertEquals(correoOriginal, correoExtraido);
    }

    @Test
    @DisplayName("Tokens de usuarios distintos deben ser distintos")
    void tokensDeUsuariosDistintos_debenSerDistintos() {
        String token1 = utilJwt.generarToken("usuario1@test.com", 1L);
        String token2 = utilJwt.generarToken("usuario2@test.com", 2L);

        assertNotEquals(token1, token2);
    }
}
