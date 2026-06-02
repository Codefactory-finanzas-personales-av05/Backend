package com.finanzas.auth.infraestructura.web;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionTransaccion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaApi;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCategoria;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaHistorial;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaTransaccion;
import com.finanzas.auth.dominio.modelo.Transaccion;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoTransaccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias del ControladorTransaccion")
class ControladorTransaccionTest {

    @Mock
    private CasoDeUsoTransaccion casoDeUsoTransaccion;

    @InjectMocks
    private ControladorTransaccion controlador;

    // ------------------------------------------------------------------ Test 7
    @Test
    @DisplayName("registrar() retorna HTTP 200 con los datos de la transaccion creada")
    void registrar_debeRetornar200_conTransaccionCreada() {
        // Arrange
        PeticionTransaccion peticion = PeticionTransaccion.builder()
                .nombre("Salario").monto(new BigDecimal("2000000"))
                .tipo(Transaccion.TipoTransaccion.INGRESO)
                .movimientoEn(LocalDateTime.now()).idCategoria(1L).build();
        String correoUsuario = "usuario@test.com";

        RespuestaTransaccion transaccionEsperada = RespuestaTransaccion.builder()
                .id(10L).nombre("Salario").monto(new BigDecimal("2000000"))
                .tipo("INGRESO").build();
        when(casoDeUsoTransaccion.registrar(peticion, correoUsuario)).thenReturn(transaccionEsperada);

        // Act
        ResponseEntity<RespuestaApi<RespuestaTransaccion>> respuesta =
                controlador.registrar(peticion, correoUsuario);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(10L, respuesta.getBody().getData().getId());
        assertEquals("Salario", respuesta.getBody().getData().getNombre());
    }

    // ------------------------------------------------------------------ Test 8
    @Test
    @DisplayName("historial() retorna HTTP 200 con datos de paginacion y balance financiero")
    void historial_debeRetornar200_conHistorialPaginado() {
        // Arrange
        String correoUsuario = "usuario@test.com";
        int pagina = 0, tamano = 10;

        RespuestaHistorial historialEsperado = RespuestaHistorial.builder()
                .paginaActual(0).totalPaginas(3).totalElementos(25L)
                .totalIngresos(new BigDecimal("5000000"))
                .totalGastos(new BigDecimal("2000000"))
                .balanceActual(new BigDecimal("3000000"))
                .transacciones(List.of()).build();
        when(casoDeUsoTransaccion.obtenerHistorial(correoUsuario, pagina, tamano))
                .thenReturn(historialEsperado);

        // Act
        ResponseEntity<RespuestaApi<RespuestaHistorial>> respuesta =
                controlador.historial(correoUsuario, pagina, tamano);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(3, respuesta.getBody().getData().getTotalPaginas());
        assertEquals(new BigDecimal("3000000"), respuesta.getBody().getData().getBalanceActual());
    }

    // ------------------------------------------------------------------ Test 9
    @Test
    @DisplayName("categorias() retorna HTTP 200 con la lista completa de categorias del usuario")
    void categorias_debeRetornar200_conListaDeCategorias() {
        // Arrange
        String correoUsuario = "usuario@test.com";
        List<RespuestaCategoria> categoriasEsperadas = List.of(
                RespuestaCategoria.builder().idCategoria(1L).nombre("Alimentacion").tipo("GASTO").build(),
                RespuestaCategoria.builder().idCategoria(2L).nombre("Salario").tipo("INGRESO").build()
        );
        when(casoDeUsoTransaccion.obtenerCategorias(correoUsuario)).thenReturn(categoriasEsperadas);

        // Act
        ResponseEntity<RespuestaApi<List<RespuestaCategoria>>> respuesta =
                controlador.categorias(correoUsuario);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(2, respuesta.getBody().getData().size());
        assertEquals("Alimentacion", respuesta.getBody().getData().get(0).getNombre());
    }

    // ----------------------------------------------------------------- Test 10
    @Test
    @DisplayName("historial() delega al caso de uso con los parametros de paginacion exactos")
    void historial_debeDelegarConPaginacionCorrecta() {
        // Arrange
        String correoUsuario = "usuario@test.com";
        int pagina = 2, tamano = 5;
        when(casoDeUsoTransaccion.obtenerHistorial(correoUsuario, pagina, tamano))
                .thenReturn(RespuestaHistorial.builder().transacciones(List.of()).build());

        // Act
        controlador.historial(correoUsuario, pagina, tamano);

        // Assert
        verify(casoDeUsoTransaccion, times(1)).obtenerHistorial(correoUsuario, 2, 5);
    }
}
