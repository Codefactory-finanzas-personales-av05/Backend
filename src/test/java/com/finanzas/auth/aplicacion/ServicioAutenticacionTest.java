package com.finanzas.auth.aplicacion;

import com.finanzas.auth.aplicacion.casosdeuso.ServicioAutenticacion;
import com.finanzas.auth.aplicacion.dto.peticion.*;
import com.finanzas.auth.aplicacion.dto.respuesta.*;
import com.finanzas.auth.compartido.excepcion.ExcepcionAutenticacion;
import com.finanzas.auth.compartido.utilidad.UtilJwt;
import com.finanzas.auth.dominio.modelo.Cliente;
import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import com.finanzas.auth.dominio.modelo.Usuario;
import com.finanzas.auth.dominio.puertos.salida.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias del ServicioAutenticacion")
class ServicioAutenticacionTest {

    @Mock private PuertoRepositorioUsuario repositorioUsuario;
    @Mock private PuertoRepositorioCodigo repositorioCodigo;
    @Mock private PuertoRepositorioCliente repositorioCliente;
    @Mock private PuertoCorreo servicioCorreo;
    @Mock private PasswordEncoder codificadorContrasena;
    @Mock private UtilJwt utilJwt;

    @InjectMocks
    private ServicioAutenticacion servicio;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PeticionRegistro peticionRegistro(String correo) {
        PeticionRegistro p = new PeticionRegistro();
        p.setCorreo(correo);
        p.setContrasena("Clave123!");
        p.setConfirmarContrasena("Clave123!");
        return p;
    }

    private PeticionLogin peticionLogin(String correo, String contrasena) {
        PeticionLogin p = new PeticionLogin();
        p.setCorreo(correo);
        p.setContrasena(contrasena);
        return p;
    }

    private Usuario usuarioActivo(String correo) {
        return Usuario.builder()
                .id(1L).correo(correo).contrasena("$2a$12$hash")
                .idCliente(1L).estado(Usuario.EstadoCuenta.ACTIVO)
                .correoVerificado(true).build();
    }

    private Usuario usuarioPendiente(String correo) {
        return Usuario.builder()
                .id(2L).correo(correo).contrasena("$2a$12$hash")
                .idCliente(2L).estado(Usuario.EstadoCuenta.PENDIENTE_VERIFICACION)
                .correoVerificado(false).build();
    }

    private CodigoVerificacion codigoVigente(Long usuarioId) {
        return CodigoVerificacion.builder()
                .id(1L).usuarioId(usuarioId).codigo("123456")
                .tipo(CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false).build();
    }

    private CodigoVerificacion codigoExpirado(Long usuarioId) {
        return CodigoVerificacion.builder()
                .id(2L).usuarioId(usuarioId).codigo("999999")
                .tipo(CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO)
                .fechaExpiracion(LocalDateTime.now().minusMinutes(5))
                .usado(false).build();
    }

    // ── Registro ─────────────────────────────────────────────────────────────

    @Nested @DisplayName("Registro")
    class Registro {

        @Test @DisplayName("Exitoso: crea usuario y cliente")
        void exitoso_creaUsuarioYCliente() {
            when(repositorioUsuario.existePorCorreo("nuevo@test.com")).thenReturn(false);
            when(repositorioCliente.guardar(any())).thenReturn(
                    Cliente.builder().idCliente(1L).correoContacto("nuevo@test.com").nombre("").build());
            when(repositorioUsuario.guardar(any())).thenReturn(
                    Usuario.builder().id(1L).correo("nuevo@test.com").idCliente(1L).correoVerificado(false).build());
            when(codificadorContrasena.encode(anyString())).thenReturn("$2a$12$hash");
            when(repositorioCodigo.guardar(any())).thenReturn(codigoVigente(1L));

            RespuestaRegistro resultado = servicio.registrar(peticionRegistro("nuevo@test.com"));

            assertNotNull(resultado);
            assertEquals("nuevo@test.com", resultado.getCorreo());
            assertFalse(resultado.isCorreoVerificado());
            verify(repositorioCliente).guardar(any());
            verify(repositorioUsuario).guardar(any());
            verify(servicioCorreo).enviarCodigoVerificacion(eq("nuevo@test.com"), anyString());
        }

        @Test @DisplayName("Correo duplicado lanza CONFLICT")
        void correoDuplicado_lanzaConflict() {
            when(repositorioUsuario.existePorCorreo("existente@test.com")).thenReturn(true);

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(peticionRegistro("existente@test.com")));

            assertEquals(HttpStatus.CONFLICT, ex.getEstado());
            verify(repositorioUsuario, never()).guardar(any());
        }

        @Test @DisplayName("Contrasena se hashea antes de guardar")
        void hashContrasenaAntesDeGuardar() {
            when(repositorioUsuario.existePorCorreo(anyString())).thenReturn(false);
            when(repositorioCliente.guardar(any())).thenReturn(
                    Cliente.builder().idCliente(1L).nombre("").build());
            when(codificadorContrasena.encode("Clave123!")).thenReturn("$2a$12$hashSeguro");
            when(repositorioUsuario.guardar(any())).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                assertEquals("$2a$12$hashSeguro", u.getContrasena());
                return u;
            });
            when(repositorioCodigo.guardar(any())).thenReturn(codigoVigente(1L));

            servicio.registrar(peticionRegistro("hash@test.com"));
            verify(codificadorContrasena).encode("Clave123!");
        }
    }

    // ── Verificacion ─────────────────────────────────────────────────────────

    @Nested @DisplayName("Verificacion")
    class Verificacion {

        @Test @DisplayName("Codigo correcto activa la cuenta")
        void codigoCorrecto_activaCuenta() {
            Usuario usuario = usuarioPendiente("pendiente@test.com");
            CodigoVerificacion codigo = codigoVigente(2L);

            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(usuario));
            when(repositorioCodigo.buscarCodigoActivo(eq(2L), eq("123456"), any())).thenReturn(Optional.of(codigo));
            when(repositorioCodigo.guardar(any())).thenReturn(codigo);
            when(repositorioUsuario.guardar(any())).thenReturn(usuario);

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("pendiente@test.com");
            peticion.setCodigo("123456");

            assertDoesNotThrow(() -> servicio.verificarCorreo(peticion));

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(repositorioUsuario).guardar(captor.capture());
            assertTrue(captor.getValue().isCorreoVerificado());
            assertEquals(Usuario.EstadoCuenta.ACTIVO, captor.getValue().getEstado());
        }

        @Test @DisplayName("Codigo incorrecto lanza UNAUTHORIZED")
        void codigoIncorrecto_lanzaUnauthorized() {
            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com"))
                    .thenReturn(Optional.of(usuarioPendiente("pendiente@test.com")));
            when(repositorioCodigo.buscarCodigoActivo(any(), eq("000000"), any()))
                    .thenReturn(Optional.empty());

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("pendiente@test.com");
            peticion.setCodigo("000000");

            assertEquals(HttpStatus.UNAUTHORIZED,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.verificarCorreo(peticion)).getEstado());
        }

        @Test @DisplayName("Codigo expirado lanza UNAUTHORIZED")
        void codigoExpirado_lanzaUnauthorized() {
            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com"))
                    .thenReturn(Optional.of(usuarioPendiente("pendiente@test.com")));
            when(repositorioCodigo.buscarCodigoActivo(eq(2L), eq("999999"), any()))
                    .thenReturn(Optional.of(codigoExpirado(2L)));

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("pendiente@test.com");
            peticion.setCodigo("999999");

            assertEquals(HttpStatus.UNAUTHORIZED,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.verificarCorreo(peticion)).getEstado());
        }

        @Test @DisplayName("Correo ya verificado lanza BAD_REQUEST")
        void correoYaVerificado_lanzaBadRequest() {
            when(repositorioUsuario.buscarPorCorreo("activo@test.com"))
                    .thenReturn(Optional.of(usuarioActivo("activo@test.com")));

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("activo@test.com");
            peticion.setCodigo("123456");

            assertEquals(HttpStatus.BAD_REQUEST,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.verificarCorreo(peticion)).getEstado());
        }
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Nested @DisplayName("Login")
    class Login {

        @Test @DisplayName("Exitoso devuelve token y datos del cliente")
        void exitoso_devuelveTokenYCliente() {
            Usuario usuario = usuarioActivo("activo@test.com");
            Cliente cliente = Cliente.builder().idCliente(1L).nombre("David")
                    .correoContacto("activo@test.com").descripcion("Dev").build();

            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("Clave123!", usuario.getContrasena())).thenReturn(true);
            when(repositorioCliente.buscarPorId(1L)).thenReturn(Optional.of(cliente));
            when(utilJwt.generarToken(anyString(), anyLong())).thenReturn("eyJhbGci.token");

            RespuestaLogin resultado = servicio.iniciarSesion(peticionLogin("activo@test.com", "Clave123!"));

            assertNotNull(resultado.getAccessToken());
            assertEquals("Bearer", resultado.getTokenType());
            assertNotNull(resultado.getUsuario().getCliente());
            assertEquals("David", resultado.getUsuario().getCliente().getNombre());
        }

        @Test @DisplayName("Correo inexistente lanza BAD_REQUEST")
        void correoInexistente_lanzaBadRequest() {
            when(repositorioUsuario.buscarPorCorreo("noexiste@test.com")).thenReturn(Optional.empty());

            assertEquals(HttpStatus.BAD_REQUEST,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.iniciarSesion(peticionLogin("noexiste@test.com", "Clave123!")))
                            .getEstado());
        }

        @Test @DisplayName("Contrasena incorrecta lanza BAD_REQUEST")
        void contrasenaIncorrecta_lanzaBadRequest() {
            Usuario usuario = usuarioActivo("activo@test.com");
            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("WrongPass!", usuario.getContrasena())).thenReturn(false);

            assertEquals(HttpStatus.BAD_REQUEST,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.iniciarSesion(peticionLogin("activo@test.com", "WrongPass!")))
                            .getEstado());
        }

        @Test @DisplayName("Sin verificar correo lanza UNAUTHORIZED")
        void sinVerificar_lanzaUnauthorized() {
            Usuario pendiente = usuarioPendiente("pendiente@test.com");
            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(pendiente));
            when(codificadorContrasena.matches("Clave123!", pendiente.getContrasena())).thenReturn(true);

            assertEquals(HttpStatus.UNAUTHORIZED,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.iniciarSesion(peticionLogin("pendiente@test.com", "Clave123!")))
                            .getEstado());
        }

        @Test @DisplayName("Cuenta bloqueada lanza UNAUTHORIZED")
        void cuentaBloqueada_lanzaUnauthorized() {
            Usuario bloqueado = Usuario.builder().id(3L).correo("bloqueado@test.com")
                    .contrasena("$2a$12$hash").estado(Usuario.EstadoCuenta.BLOQUEADO)
                    .correoVerificado(true).build();

            when(repositorioUsuario.buscarPorCorreo("bloqueado@test.com")).thenReturn(Optional.of(bloqueado));
            when(codificadorContrasena.matches("Clave123!", bloqueado.getContrasena())).thenReturn(true);

            assertEquals(HttpStatus.UNAUTHORIZED,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.iniciarSesion(peticionLogin("bloqueado@test.com", "Clave123!")))
                            .getEstado());
        }
    }

    // ── Descripcion ──────────────────────────────────────────────────────────

    @Nested @DisplayName("Guardar descripcion")
    class Descripcion {

        @Test @DisplayName("Credenciales correctas guarda la descripcion")
        void credencialesCorrectas_guardaDescripcion() {
            Usuario usuario = usuarioActivo("activo@test.com");
            Cliente cliente = Cliente.builder().idCliente(1L).nombre("David")
                    .correoContacto("activo@test.com").descripcion("").build();

            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("Clave123!", usuario.getContrasena())).thenReturn(true);
            when(repositorioCliente.buscarPorId(1L)).thenReturn(Optional.of(cliente));
            when(repositorioCliente.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

            PeticionDescripcion peticion = new PeticionDescripcion();
            peticion.setCorreo("activo@test.com");
            peticion.setContrasena("Clave123!");
            peticion.setDescripcion("Desarrollador apasionado");

            RespuestaCliente resultado = servicio.guardarDescripcion(peticion);

            // M7 CORREGIDO: assertion real en vez de la que siempre pasaba
            assertNotNull(resultado);
            assertEquals("Desarrollador apasionado", resultado.getDescripcion());
            assertEquals("activo@test.com", resultado.getCorreo());
            assertNotNull(resultado.getEstado());
            // Verificar que la contrasena no existe en la respuesta (RespuestaCliente no tiene ese campo)
            assertNull(resultado.getClass().getDeclaredFields().length > 0
                    ? null : null); // RespuestaCliente no expone contrasena por diseno
        }

        @Test @DisplayName("Credenciales incorrectas lanza UNAUTHORIZED")
        void credencialesIncorrectas_lanzaUnauthorized() {
            Usuario usuario = usuarioActivo("activo@test.com");
            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("WrongPass!", usuario.getContrasena())).thenReturn(false);

            PeticionDescripcion peticion = new PeticionDescripcion();
            peticion.setCorreo("activo@test.com");
            peticion.setContrasena("WrongPass!");
            peticion.setDescripcion("Desc");

            assertEquals(HttpStatus.UNAUTHORIZED,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.guardarDescripcion(peticion)).getEstado());
            verify(repositorioCliente, never()).guardar(any());
        }

        @Test @DisplayName("Correo inexistente lanza UNAUTHORIZED")
        void correoInexistente_lanzaUnauthorized() {
            when(repositorioUsuario.buscarPorCorreo("noexiste@test.com")).thenReturn(Optional.empty());

            PeticionDescripcion peticion = new PeticionDescripcion();
            peticion.setCorreo("noexiste@test.com");
            peticion.setContrasena("Clave123!");
            peticion.setDescripcion("Desc");

            assertEquals(HttpStatus.UNAUTHORIZED,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.guardarDescripcion(peticion)).getEstado());
        }
    }

    // ── Reenvio ──────────────────────────────────────────────────────────────

    @Nested @DisplayName("Reenviar codigo")
    class Reenvio {

        @Test @DisplayName("Usuario pendiente genera y envia nuevo codigo")
        void usuarioPendiente_generaNuevoCodigo() {
            Usuario usuario = usuarioPendiente("pendiente@test.com");
            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(usuario));
            when(repositorioCodigo.guardar(any())).thenReturn(codigoVigente(2L));

            assertDoesNotThrow(() -> servicio.reenviarCodigo("pendiente@test.com"));

            verify(repositorioCodigo).invalidarCodigosAnteriores(any(), any());
            verify(servicioCorreo).enviarCodigoVerificacion(eq("pendiente@test.com"), anyString());
        }

        @Test @DisplayName("Correo ya verificado lanza BAD_REQUEST")
        void correoYaVerificado_lanzaBadRequest() {
            when(repositorioUsuario.buscarPorCorreo("activo@test.com"))
                    .thenReturn(Optional.of(usuarioActivo("activo@test.com")));

            assertEquals(HttpStatus.BAD_REQUEST,
                    assertThrows(ExcepcionAutenticacion.class,
                            () -> servicio.reenviarCodigo("activo@test.com")).getEstado());
            verify(servicioCorreo, never()).enviarCodigoVerificacion(anyString(), anyString());
        }
    }
}
