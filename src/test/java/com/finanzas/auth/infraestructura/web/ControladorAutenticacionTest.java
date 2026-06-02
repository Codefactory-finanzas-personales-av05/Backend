package com.finanzas.auth.infraestructura.web;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionDescripcion;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionLogin;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionRegistro;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionVerificacion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaApi;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCliente;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaLogin;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaRegistro;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoAutenticacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias del ControladorAutenticacion")
class ControladorAutenticacionTest {

    @Mock
    private CasoDeUsoAutenticacion casoDeUsoAuth;

    @InjectMocks
    private ControladorAutenticacion controlador;

    // ------------------------------------------------------------------ Test 1
    @Test
    @DisplayName("registro() retorna HTTP 200 con los datos del usuario registrado")
    void registro_debeRetornar200_cuandoRegistroExitoso() {
        // Arrange
        PeticionRegistro peticion = new PeticionRegistro();
        peticion.setCorreo("usuario@test.com");
        peticion.setContrasena("Clave1234!");
        peticion.setConfirmarContrasena("Clave1234!");

        RespuestaRegistro datosEsperados = RespuestaRegistro.builder()
                .id(1L).correo("usuario@test.com").correoVerificado(false).build();
        when(casoDeUsoAuth.registrar(peticion)).thenReturn(datosEsperados);

        // Act
        ResponseEntity<RespuestaApi<RespuestaRegistro>> respuesta = controlador.registro(peticion);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(200, respuesta.getBody().getStatus());
        assertEquals(datosEsperados, respuesta.getBody().getData());
    }

    // ------------------------------------------------------------------ Test 2
    @Test
    @DisplayName("registro() delega exactamente una vez al caso de uso")
    void registro_debeDelegarAlCasoDeUso_unaVez() {
        // Arrange
        PeticionRegistro peticion = new PeticionRegistro();
        peticion.setCorreo("usuario@test.com");
        when(casoDeUsoAuth.registrar(peticion)).thenReturn(RespuestaRegistro.builder().build());

        // Act
        controlador.registro(peticion);

        // Assert
        verify(casoDeUsoAuth, times(1)).registrar(peticion);
    }

    // ------------------------------------------------------------------ Test 3
    @Test
    @DisplayName("verificarCorreo() retorna HTTP 201 cuando el codigo es valido")
    void verificarCorreo_debeRetornar201_cuandoCodigoValido() {
        // Arrange
        PeticionVerificacion peticion = new PeticionVerificacion();
        peticion.setCorreo("usuario@test.com");
        peticion.setCodigo("123456");
        doNothing().when(casoDeUsoAuth).verificarCorreo(peticion);

        // Act
        ResponseEntity<RespuestaApi<Void>> respuesta = controlador.verificarCorreo(peticion);

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(201, respuesta.getBody().getStatus());
        assertNull(respuesta.getBody().getData());
    }

    // ------------------------------------------------------------------ Test 4
    @Test
    @DisplayName("login() retorna HTTP 200 con token JWT y datos del usuario")
    void login_debeRetornar200_conTokenJwt() {
        // Arrange
        PeticionLogin peticion = new PeticionLogin();
        peticion.setCorreo("usuario@test.com");
        peticion.setContrasena("Clave1234!");

        RespuestaLogin loginEsperado = RespuestaLogin.builder()
                .accessToken("jwt.token.firmado").tokenType("Bearer").expiraEn(3600L).build();
        when(casoDeUsoAuth.iniciarSesion(peticion)).thenReturn(loginEsperado);

        // Act
        ResponseEntity<RespuestaApi<RespuestaLogin>> respuesta = controlador.login(peticion);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("jwt.token.firmado", respuesta.getBody().getData().getAccessToken());
        assertEquals("Bearer", respuesta.getBody().getData().getTokenType());
    }

    // ------------------------------------------------------------------ Test 5
    @Test
    @DisplayName("guardarDescripcion() retorna HTTP 200 con los datos actualizados del cliente")
    void guardarDescripcion_debeRetornar200_conDatosCliente() {
        // Arrange
        PeticionDescripcion peticion = new PeticionDescripcion();
        peticion.setCorreo("usuario@test.com");
        peticion.setContrasena("Clave1234!");
        peticion.setDescripcion("Me gustan las finanzas personales");

        RespuestaCliente clienteEsperado = RespuestaCliente.builder()
                .correo("usuario@test.com")
                .descripcion("Me gustan las finanzas personales")
                .build();
        when(casoDeUsoAuth.guardarDescripcion(peticion)).thenReturn(clienteEsperado);

        // Act
        ResponseEntity<RespuestaApi<RespuestaCliente>> respuesta =
                controlador.guardarDescripcion(peticion);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Me gustan las finanzas personales",
                respuesta.getBody().getData().getDescripcion());
    }

    // ------------------------------------------------------------------ Test 6
    @Test
    @DisplayName("reenviarCodigo() retorna HTTP 200 con mensaje que incluye el correo destino")
    void reenviarCodigo_debeRetornar200_conMensajeConfirmacion() {
        // Arrange
        String correo = "usuario@test.com";
        doNothing().when(casoDeUsoAuth).reenviarCodigo(correo);

        // Act
        ResponseEntity<RespuestaApi<Void>> respuesta = controlador.reenviarCodigo(correo);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertTrue(respuesta.getBody().getMensaje().contains(correo));
    }
}
