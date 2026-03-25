package com.finanzas.auth;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionLogin;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionRegistro;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaRegistro;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoAutenticacion;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioUsuario;
import com.finanzas.auth.compartido.excepcion.ExcepcionAutenticacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Pruebas basicas de integracion.
 * Verificamos que los casos de uso principales funcionen correctamente.
 */
@SpringBootTest
class PruebasAutenticacion {

    @Autowired
    private CasoDeUsoAutenticacion casoDeUsoAuth;

    @Autowired
    private PuertoRepositorioUsuario repositorioUsuario;

    @Test
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
    void registrarCorreoDuplicado_debeLanzarExcepcion() {
        PeticionRegistro peticion = new PeticionRegistro();
        peticion.setCorreo("duplicado@test.com");
        peticion.setContrasena("Clave123!");
        peticion.setConfirmarContrasena("Clave123!");

        // Primer registro - debe funcionar
        casoDeUsoAuth.registrar(peticion);

        // Segundo registro con el mismo correo - debe fallar
        assertThrows(ExcepcionAutenticacion.class, () -> casoDeUsoAuth.registrar(peticion));
    }

    @Test
    void loginSinVerificar_debeLanzarExcepcion() {
        // Registrar sin verificar
        PeticionRegistro peticionRegistro = new PeticionRegistro();
        peticionRegistro.setCorreo("sinverificar@test.com");
        peticionRegistro.setContrasena("Clave123!");
        peticionRegistro.setConfirmarContrasena("Clave123!");
        casoDeUsoAuth.registrar(peticionRegistro);

        // Intentar login sin verificar el correo
        PeticionLogin peticionLogin = new PeticionLogin();
        peticionLogin.setCorreo("sinverificar@test.com");
        peticionLogin.setContrasena("Clave123!");

        assertThrows(ExcepcionAutenticacion.class, () -> casoDeUsoAuth.iniciarSesion(peticionLogin));
    }
}
