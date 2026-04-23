package com.finanzas.auth;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionLogin;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionRegistro;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaRegistro;
import com.finanzas.auth.compartido.excepcion.ExcepcionAutenticacion;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoAutenticacion;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Pruebas de integracion — levantan el contexto completo de Spring Boot con H2.
 *
 * @ActiveProfiles("test") activa application-test.yml que fuerza H2 en memoria.
 * Funciona igual sin importar si en produccion se usa PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PruebasAutenticacion {

    @Autowired
    private CasoDeUsoAutenticacion casoDeUsoAuth;

    @Test
    @Order(1)
    @DisplayName("Registro de usuario nuevo debe crear la cuenta correctamente")
    void registrarUsuarioNuevo_debeCrearLaCuenta() {
        PeticionRegistro peticion = new PeticionRegistro();
        peticion.setCorreo("prueba@test.com");
        peticion.setContrasena("Clave123!");
        peticion.setConfirmarContrasena("Clave123!");

        RespuestaRegistro respuesta = casoDeUsoAuth.registrar(peticion);

        assertNotNull(respuesta.getId());
        assertEquals("prueba@test.com", respuesta.getCorreo());
        assertFalse(respuesta.isCorreoVerificado());
    }

    @Test
    @Order(2)
    @DisplayName("Registrar correo duplicado debe lanzar excepcion CONFLICT")
    void registrarCorreoDuplicado_debeLanzarExcepcion() {
        PeticionRegistro peticion = new PeticionRegistro();
        peticion.setCorreo("duplicado@test.com");
        peticion.setContrasena("Clave123!");
        peticion.setConfirmarContrasena("Clave123!");

        casoDeUsoAuth.registrar(peticion);

        assertThrows(ExcepcionAutenticacion.class, () -> casoDeUsoAuth.registrar(peticion));
    }

    @Test
    @Order(3)
    @DisplayName("Login sin verificar el correo debe lanzar excepcion")
    void loginSinVerificar_debeLanzarExcepcion() {
        PeticionRegistro peticionRegistro = new PeticionRegistro();
        peticionRegistro.setCorreo("sinverificar@test.com");
        peticionRegistro.setContrasena("Clave123!");
        peticionRegistro.setConfirmarContrasena("Clave123!");
        casoDeUsoAuth.registrar(peticionRegistro);

        PeticionLogin peticionLogin = new PeticionLogin();
        peticionLogin.setCorreo("sinverificar@test.com");
        peticionLogin.setContrasena("Clave123!");

        assertThrows(ExcepcionAutenticacion.class,
                () -> casoDeUsoAuth.iniciarSesion(peticionLogin));
    }
}
