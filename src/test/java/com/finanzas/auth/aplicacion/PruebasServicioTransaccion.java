package com.finanzas.auth.aplicacion;

import com.finanzas.auth.aplicacion.casosdeuso.ServicioTransaccion;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionTransaccion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaHistorial;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaTransaccion;
import com.finanzas.auth.compartido.excepcion.ExcepcionAutenticacion;
import com.finanzas.auth.dominio.modelo.Categoria;
import com.finanzas.auth.dominio.modelo.Transaccion;
import com.finanzas.auth.dominio.modelo.Usuario;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCategoria;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioTransaccion;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioUsuario;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias del ServicioTransaccion")
class PruebasServicioTransaccion {

    @Mock private PuertoRepositorioTransaccion repositorioTransaccion;
    @Mock private PuertoRepositorioCategoria repositorioCategoria;
    @Mock private PuertoRepositorioUsuario repositorioUsuario;

    @InjectMocks private ServicioTransaccion servicio;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Usuario usuarioActivo() {
        return Usuario.builder()
                .id(1L).correo("david@test.com")
                .idCliente(1L).estado(Usuario.EstadoCuenta.ACTIVO)
                .correoVerificado(true).build();
    }

    private Categoria categoriaIngreso() {
        return Categoria.builder()
                .idCategoria(10L).nombre("Salario").icono("💼")
                .tipo(Categoria.TipoCategoria.INGRESO).idCliente(1L).build();
    }

    private Categoria categoriaGasto() {
        return Categoria.builder()
                .idCategoria(20L).nombre("Comida").icono("🍔")
                .tipo(Categoria.TipoCategoria.GASTO).idCliente(1L).build();
    }

    private PeticionTransaccion peticionIngreso() {
        return PeticionTransaccion.builder()
                .nombre("Salario mensual").monto(new BigDecimal("3000000"))
                .movimientoEn(LocalDateTime.now()).tipo(Transaccion.TipoTransaccion.INGRESO)
                .idCategoria(10L).build();
    }

    private PeticionTransaccion peticionGasto() {
        return PeticionTransaccion.builder()
                .nombre("Almuerzo").monto(new BigDecimal("15000"))
                .movimientoEn(LocalDateTime.now()).tipo(Transaccion.TipoTransaccion.GASTO)
                .idCategoria(20L).build();
    }

    private Transaccion transaccionGuardada(PeticionTransaccion peticion, Long idCategoria) {
        return Transaccion.builder()
                .id(100L).nombre(peticion.getNombre()).monto(peticion.getMonto())
                .movimientoEn(peticion.getMovimientoEn()).tipo(peticion.getTipo())
                .idCliente(1L).idCategoria(idCategoria)
                .nombreCategoria("Salario").iconoCategoria("💼")
                .creadoEn(LocalDateTime.now()).build();
    }

    // ── HU-03: Registrar ingreso ──────────────────────────────────────────────

    @Nested @DisplayName("HU-03 — Registrar ingreso")
    class RegistrarIngreso {

        @Test @DisplayName("Ingreso exitoso devuelve la transaccion con balance actualizado")
        void ingresoExitoso_devuelveTransaccionYBalance() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(10L))
                    .thenReturn(Optional.of(categoriaIngreso()));
            when(repositorioTransaccion.guardar(any()))
                    .thenReturn(transaccionGuardada(peticionIngreso(), 10L));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.INGRESO))
                    .thenReturn(new BigDecimal("3000000"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.GASTO))
                    .thenReturn(BigDecimal.ZERO);

            RespuestaTransaccion resultado = servicio.registrar(peticionIngreso(), "david@test.com");

            assertNotNull(resultado);
            assertEquals(new BigDecimal("3000000"), resultado.getBalanceActual());
            assertEquals("INGRESO", resultado.getTipo());
            verify(repositorioTransaccion).guardar(any());
        }

        @Test @DisplayName("Categoria de otro cliente lanza FORBIDDEN")
        void categoriaDeOtroCliente_lanzaForbidden() {
            Categoria categoriaAjena = Categoria.builder()
                    .idCategoria(10L).nombre("Ajena").tipo(Categoria.TipoCategoria.INGRESO)
                    .idCliente(99L).build();   // cliente diferente

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(10L))
                    .thenReturn(Optional.of(categoriaAjena));

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(peticionIngreso(), "david@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getEstado());
            verify(repositorioTransaccion, never()).guardar(any());
        }

        @Test @DisplayName("Tipo no coincide con categoria lanza BAD_REQUEST")
        void tipoNoCoincide_lanzaBadRequest() {
            // Intenta registrar un INGRESO en una categoria de GASTO
            PeticionTransaccion peticionMal = PeticionTransaccion.builder()
                    .nombre("Error").monto(new BigDecimal("1000"))
                    .movimientoEn(LocalDateTime.now())
                    .tipo(Transaccion.TipoTransaccion.INGRESO)
                    .idCategoria(20L).build();

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(20L))
                    .thenReturn(Optional.of(categoriaGasto())); // tipo GASTO

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(peticionMal, "david@test.com"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        }

        @Test @DisplayName("Categoria inexistente lanza NOT_FOUND")
        void categoriaInexistente_lanzaNotFound() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(999L)).thenReturn(Optional.empty());

            PeticionTransaccion peticion = peticionIngreso();
            peticion.setIdCategoria(999L);

            assertEquals(HttpStatus.NOT_FOUND,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.registrar(peticion, "david@test.com")).getEstado());
        }
    }

    // ── HU-04: Registrar gasto ────────────────────────────────────────────────

    @Nested @DisplayName("HU-04 — Registrar gasto")
    class RegistrarGasto {

        @Test @DisplayName("Gasto exitoso reduce el balance")
        void gastoExitoso_reduceBalance() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(20L))
                    .thenReturn(Optional.of(categoriaGasto()));
            when(repositorioTransaccion.guardar(any()))
                    .thenReturn(transaccionGuardada(peticionGasto(), 20L));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.INGRESO))
                    .thenReturn(new BigDecimal("3000000"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.GASTO))
                    .thenReturn(new BigDecimal("15000"));

            RespuestaTransaccion resultado = servicio.registrar(peticionGasto(), "david@test.com");

            assertNotNull(resultado);
            // balance = 3000000 - 15000 = 2985000
            assertEquals(new BigDecimal("2985000"), resultado.getBalanceActual());
        }
    }

    // ── HU-05: Historial ──────────────────────────────────────────────────────

    @Nested @DisplayName("HU-05 — Historial paginado")
    class Historial {

        @Test @DisplayName("Historial devuelve transacciones paginadas con balance")
        void historial_devuelvePaginaConBalance() {
            Transaccion t = Transaccion.builder()
                    .id(1L).nombre("Salario").monto(new BigDecimal("3000000"))
                    .movimientoEn(LocalDateTime.now())
                    .tipo(Transaccion.TipoTransaccion.INGRESO)
                    .idCliente(1L).nombreCategoria("Salario").iconoCategoria("💼").build();

            Page<Transaccion> pagina = new PageImpl<>(List.of(t));

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioTransaccion.buscarPorIdCliente(eq(1L), any()))
                    .thenReturn(pagina);
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.INGRESO))
                    .thenReturn(new BigDecimal("3000000"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.GASTO))
                    .thenReturn(BigDecimal.ZERO);

            RespuestaHistorial resultado = servicio.obtenerHistorial("david@test.com", 0, 10);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTransacciones().size());
            assertEquals(new BigDecimal("3000000"), resultado.getTotalIngresos());
            assertEquals(BigDecimal.ZERO, resultado.getTotalGastos());
            assertEquals(new BigDecimal("3000000"), resultado.getBalanceActual());
        }

        @Test @DisplayName("Historial vacio devuelve balance en cero")
        void historialVacio_balanceEnCero() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioTransaccion.buscarPorIdCliente(eq(1L), any()))
                    .thenReturn(new PageImpl<>(List.of()));
            when(repositorioTransaccion.sumarMontosPorTipo(any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            RespuestaHistorial resultado = servicio.obtenerHistorial("david@test.com", 0, 10);

            assertTrue(resultado.getTransacciones().isEmpty());
            assertEquals(BigDecimal.ZERO, resultado.getBalanceActual());
        }
    }
}
