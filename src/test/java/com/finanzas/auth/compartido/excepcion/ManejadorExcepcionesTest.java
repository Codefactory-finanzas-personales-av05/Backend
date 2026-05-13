package com.finanzas.auth.compartido.excepcion;

import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Pruebas unitarias del ManejadorExcepciones")
class ManejadorExcepcionesTest {

    private ManejadorExcepciones manejador;

    @BeforeEach
    void setUp() {
        manejador = new ManejadorExcepciones();
    }

    // ----------------------------------------------------------------- Test 11
    @Test
    @DisplayName("manejarValidacion() retorna HTTP 400 con mapa de errores por campo")
    void manejarValidacion_debeRetornar400_conMapaDeErrores() {
        // Arrange
        FieldError campoError = new FieldError("peticion", "correo", "El correo es obligatorio");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(List.of(campoError));

        MethodArgumentNotValidException excepcion = mock(MethodArgumentNotValidException.class);
        when(excepcion.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<RespuestaApi<Map<String, String>>> respuesta =
                manejador.manejarValidacion(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(400, respuesta.getBody().getStatus());
        assertTrue(respuesta.getBody().getData().containsKey("correo"));
        assertEquals("El correo es obligatorio", respuesta.getBody().getData().get("correo"));
    }

    // ----------------------------------------------------------------- Test 12
    @Test
    @DisplayName("manejarExcepcionAuth() retorna 401 cuando las credenciales son incorrectas")
    void manejarExcepcionAuth_debeRetornar401_cuandoNoAutorizado() {
        // Arrange
        ExcepcionAutenticacion excepcion = new ExcepcionAutenticacion(
                "Credenciales incorrectas", HttpStatus.UNAUTHORIZED);

        // Act
        ResponseEntity<RespuestaApi<Void>> respuesta = manejador.manejarExcepcionAuth(excepcion);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals(401, respuesta.getBody().getStatus());
        assertEquals("Credenciales incorrectas", respuesta.getBody().getMensaje());
    }

    // ----------------------------------------------------------------- Test 13
    @Test
    @DisplayName("manejarExcepcionAuth() retorna 409 cuando el correo ya esta registrado")
    void manejarExcepcionAuth_debeRetornar409_cuandoConflicto() {
        // Arrange
        ExcepcionAutenticacion excepcion = new ExcepcionAutenticacion(
                "El correo ya esta registrado", HttpStatus.CONFLICT);

        // Act
        ResponseEntity<RespuestaApi<Void>> respuesta = manejador.manejarExcepcionAuth(excepcion);

        // Assert
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals(409, respuesta.getBody().getStatus());
        assertNull(respuesta.getBody().getData());
    }

    // ----------------------------------------------------------------- Test 14
    @Test
    @DisplayName("manejarErrorGeneral() retorna HTTP 500 sin exponer detalles del error interno")
    void manejarErrorGeneral_debeRetornar500_sinExponerDetalles() {
        // Arrange
        Exception excepcion = new RuntimeException("Error de conexion a la base de datos");

        // Act
        ResponseEntity<RespuestaApi<Void>> respuesta = manejador.manejarErrorGeneral(excepcion);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertEquals(500, respuesta.getBody().getStatus());
        assertFalse(respuesta.getBody().getMensaje().contains("base de datos"));
    }
}
