package com.finanzas.auth.aplicacion;

import com.finanzas.auth.aplicacion.casosdeuso.ServicioTransaccion;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionTransaccion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCategoria;
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
@DisplayName("Pruebas unitarias del ServicioTransaccion — HU-03, HU-04, HU-05")
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
                .nombre("Salario mensual")
                .monto(new BigDecimal("3000000"))
                .movimientoEn(LocalDateTime.now())
                .tipo(Transaccion.TipoTransaccion.INGRESO)
                .idCategoria(10L).build();
    }

    private PeticionTransaccion peticionGasto() {
        return PeticionTransaccion.builder()
                .nombre("Almuerzo")
                .monto(new BigDecimal("15000"))
                .movimientoEn(LocalDateTime.now())
                .tipo(Transaccion.TipoTransaccion.GASTO)
                .idCategoria(20L).build();
    }

    private Transaccion transaccionGuardada(PeticionTransaccion p, String nombreCat, String iconoCat) {
        return Transaccion.builder()
                .id(100L).nombre(p.getNombre()).monto(p.getMonto())
                .movimientoEn(p.getMovimientoEn()).tipo(p.getTipo())
                .idCliente(1L).idCategoria(p.getIdCategoria())
                .nombreCategoria(nombreCat).iconoCategoria(iconoCat)
                .creadoEn(LocalDateTime.now()).build();
    }

    // ── HU-03: Registrar ingreso ──────────────────────────────────────────────

    @Nested
    @DisplayName("HU-03 — Registrar ingreso")
    class RegistrarIngreso {

        @Test
        @DisplayName("Ingreso exitoso devuelve la transaccion con balance actualizado")
        void ingresoExitoso_devuelveTransaccionYBalance() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(10L))
                    .thenReturn(Optional.of(categoriaIngreso()));
            when(repositorioTransaccion.guardar(any()))
                    .thenReturn(transaccionGuardada(peticionIngreso(), "Salario", "💼"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.INGRESO))
                    .thenReturn(new BigDecimal("3000000"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.GASTO))
                    .thenReturn(BigDecimal.ZERO);

            RespuestaTransaccion resultado = servicio.registrar(peticionIngreso(), "david@test.com");

            assertNotNull(resultado);
            assertEquals(new BigDecimal("3000000"), resultado.getBalanceActual());
            assertEquals("INGRESO", resultado.getTipo());
            assertEquals("Salario", resultado.getNombreCategoria());
            verify(repositorioTransaccion).guardar(any());
        }

        @Test
        @DisplayName("Categoria de otro cliente lanza FORBIDDEN")
        void categoriaDeOtroCliente_lanzaForbidden() {
            Categoria categoriaAjena = Categoria.builder()
                    .idCategoria(10L).nombre("Ajena")
                    .tipo(Categoria.TipoCategoria.INGRESO)
                    .idCliente(99L).build();

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(10L))
                    .thenReturn(Optional.of(categoriaAjena));

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(peticionIngreso(), "david@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getEstado());
            verify(repositorioTransaccion, never()).guardar(any());
        }

        @Test
        @DisplayName("Tipo no coincide con categoria lanza BAD_REQUEST")
        void tipoNoCoincideConCategoria_lanzaBadRequest() {
            // Intentar registrar INGRESO en una categoria de GASTO
            PeticionTransaccion peticionMal = PeticionTransaccion.builder()
                    .nombre("Error de tipo").monto(new BigDecimal("1000"))
                    .movimientoEn(LocalDateTime.now())
                    .tipo(Transaccion.TipoTransaccion.INGRESO)
                    .idCategoria(20L).build();

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(20L))
                    .thenReturn(Optional.of(categoriaGasto()));

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(peticionMal, "david@test.com"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
            verify(repositorioTransaccion, never()).guardar(any());
        }

        @Test
        @DisplayName("Categoria inexistente lanza NOT_FOUND")
        void categoriaInexistente_lanzaNotFound() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(999L))
                    .thenReturn(Optional.empty());

            PeticionTransaccion peticion = peticionIngreso();
            peticion.setIdCategoria(999L);

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(peticion, "david@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getEstado());
        }
    }

    // ── HU-04: Registrar gasto ────────────────────────────────────────────────

    @Nested
    @DisplayName("HU-04 — Registrar gasto")
    class RegistrarGasto {

        @Test
        @DisplayName("Gasto exitoso reduce el balance correctamente")
        void gastoExitoso_reduceBalance() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(20L))
                    .thenReturn(Optional.of(categoriaGasto()));
            when(repositorioTransaccion.guardar(any()))
                    .thenReturn(transaccionGuardada(peticionGasto(), "Comida", "🍔"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.INGRESO))
                    .thenReturn(new BigDecimal("3000000"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.GASTO))
                    .thenReturn(new BigDecimal("15000"));

            RespuestaTransaccion resultado = servicio.registrar(peticionGasto(), "david@test.com");

            assertNotNull(resultado);
            // balance = 3000000 - 15000 = 2985000
            assertEquals(new BigDecimal("2985000"), resultado.getBalanceActual());
            assertEquals("GASTO", resultado.getTipo());
            assertEquals("Comida", resultado.getNombreCategoria());
        }

        @Test
        @DisplayName("Gasto en categoria de ingreso lanza BAD_REQUEST")
        void gastoEnCategoriaIngreso_lanzaBadRequest() {
            PeticionTransaccion peticionMal = PeticionTransaccion.builder()
                    .nombre("Error").monto(new BigDecimal("5000"))
                    .movimientoEn(LocalDateTime.now())
                    .tipo(Transaccion.TipoTransaccion.GASTO)
                    .idCategoria(10L).build();

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorId(10L))
                    .thenReturn(Optional.of(categoriaIngreso()));

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(peticionMal, "david@test.com"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        }
    }

    // ── HU-05: Historial ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("HU-05 — Historial paginado")
    class Historial {

        @Test
        @DisplayName("Historial devuelve transacciones paginadas con balance completo")
        void historial_devuelvePaginaConBalance() {
            Transaccion t1 = Transaccion.builder()
                    .id(1L).nombre("Salario").monto(new BigDecimal("3000000"))
                    .movimientoEn(LocalDateTime.now())
                    .tipo(Transaccion.TipoTransaccion.INGRESO)
                    .idCliente(1L).nombreCategoria("Salario").iconoCategoria("💼").build();

            Transaccion t2 = Transaccion.builder()
                    .id(2L).nombre("Almuerzo").monto(new BigDecimal("15000"))
                    .movimientoEn(LocalDateTime.now().minusHours(2))
                    .tipo(Transaccion.TipoTransaccion.GASTO)
                    .idCliente(1L).nombreCategoria("Comida").iconoCategoria("🍔").build();

            Page<Transaccion> pagina = new PageImpl<>(List.of(t1, t2));

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioTransaccion.buscarPorIdCliente(eq(1L), any()))
                    .thenReturn(pagina);
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.INGRESO))
                    .thenReturn(new BigDecimal("3000000"));
            when(repositorioTransaccion.sumarMontosPorTipo(1L, Transaccion.TipoTransaccion.GASTO))
                    .thenReturn(new BigDecimal("15000"));

            RespuestaHistorial resultado = servicio.obtenerHistorial("david@test.com", 0, 10);

            assertNotNull(resultado);
            assertEquals(2, resultado.getTransacciones().size());
            assertEquals(new BigDecimal("3000000"), resultado.getTotalIngresos());
            assertEquals(new BigDecimal("15000"), resultado.getTotalGastos());
            assertEquals(new BigDecimal("2985000"), resultado.getBalanceActual());
            assertEquals(0, resultado.getPaginaActual());
        }

        @Test
        @DisplayName("Historial vacio devuelve balance en cero")
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
            assertEquals(BigDecimal.ZERO, resultado.getTotalIngresos());
            assertEquals(BigDecimal.ZERO, resultado.getTotalGastos());
        }

        @Test
        @DisplayName("Historial paginado respeta el tamano de pagina")
        void historial_respetaTamanoPagina() {
            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioTransaccion.buscarPorIdCliente(eq(1L), any()))
                    .thenReturn(new PageImpl<>(List.of()));
            when(repositorioTransaccion.sumarMontosPorTipo(any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            servicio.obtenerHistorial("david@test.com", 0, 5);

            // Verifica que se llama con pageable de tamano 5
            ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                    ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
            verify(repositorioTransaccion).buscarPorIdCliente(eq(1L), captor.capture());
            assertEquals(5, captor.getValue().getPageSize());
        }
    }

    // ── Categorias ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Categorias del cliente")
    class Categorias {

        @Test
        @DisplayName("Devuelve lista de categorias del cliente autenticado")
        void obtenerCategorias_devuelveListaDelCliente() {
            List<Categoria> lista = List.of(
                    categoriaIngreso(),
                    categoriaGasto()
            );

            when(repositorioUsuario.buscarPorCorreo("david@test.com"))
                    .thenReturn(Optional.of(usuarioActivo()));
            when(repositorioCategoria.buscarPorIdCliente(1L))
                    .thenReturn(lista);

            List<RespuestaCategoria> resultado = servicio.obtenerCategorias("david@test.com");

            assertEquals(2, resultado.size());
            assertEquals("Salario", resultado.get(0).getNombre());
            assertEquals("INGRESO", resultado.get(0).getTipo());
            assertEquals("Comida", resultado.get(1).getNombre());
            assertEquals("GASTO", resultado.get(1).getTipo());
        }

        @Test
        @DisplayName("Usuario inexistente lanza UNAUTHORIZED")
        void usuarioInexistente_lanzaUnauthorized() {
            when(repositorioUsuario.buscarPorCorreo("noexiste@test.com"))
                    .thenReturn(Optional.empty());

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.obtenerCategorias("noexiste@test.com"));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
        }
    }
}
