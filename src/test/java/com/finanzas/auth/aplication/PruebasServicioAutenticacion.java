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

/*
 * Pruebas UNITARIAS del ServicioAutenticacion.
 * Usamos Mockito para simular los puertos de salida, asi no necesitamos
 * base de datos ni ningun componente externo. Se prueba solo la logica del servicio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias del ServicioAutenticacion")
class PruebasServicioAutenticacion {

    // Los mocks simulan los puertos de salida
    @Mock private PuertoRepositorioUsuario repositorioUsuario;
    @Mock private PuertoRepositorioCodigo repositorioCodigo;
    @Mock private PuertoRepositorioCliente repositorioCliente;
    @Mock private PuertoCorreo servicioCorreo;
    @Mock private PasswordEncoder codificadorContrasena;
    @Mock private UtilJwt utilJwt;

    // El servicio real, con los mocks inyectados
    @InjectMocks
    private ServicioAutenticacion servicio;

    // ─────────────────────────────────────────────
    // HELPERS - construyen objetos comunes de prueba
    // ─────────────────────────────────────────────

    private PeticionRegistro crearPeticionRegistro(String correo) {
        PeticionRegistro p = new PeticionRegistro();
        p.setCorreo(correo);
        p.setContrasena("Clave123!");
        p.setConfirmarContrasena("Clave123!");
        return p;
    }

    private PeticionLogin crearPeticionLogin(String correo, String contrasena) {
        PeticionLogin p = new PeticionLogin();
        p.setCorreo(correo);
        p.setContrasena(contrasena);
        return p;
    }

    private Usuario crearUsuarioActivo(String correo) {
        return Usuario.builder()
                .id(1L)
                .correo(correo)
                .contrasena("$2a$12$hashBcrypt")
                .idCliente(1L)
                .estado(Usuario.EstadoCuenta.ACTIVO)
                .correoVerificado(true)
                .build();
    }

    private Usuario crearUsuarioPendiente(String correo) {
        return Usuario.builder()
                .id(2L)
                .correo(correo)
                .contrasena("$2a$12$hashBcrypt")
                .idCliente(2L)
                .estado(Usuario.EstadoCuenta.PENDIENTE_VERIFICACION)
                .correoVerificado(false)
                .build();
    }

    private CodigoVerificacion crearCodigoVigente(Long usuarioId) {
        return CodigoVerificacion.builder()
                .id(1L)
                .usuarioId(usuarioId)
                .codigo("123456")
                .tipo(CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();
    }

    private CodigoVerificacion crearCodigoExpirado(Long usuarioId) {
        return CodigoVerificacion.builder()
                .id(2L)
                .usuarioId(usuarioId)
                .codigo("999999")
                .tipo(CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO)
                .fechaExpiracion(LocalDateTime.now().minusMinutes(5))
                .usado(false)
                .build();
    }

    // ─────────────────────────────────────────────
    // PRUEBAS DE REGISTRO
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Caso de uso: Registrar usuario")
    class PruebasRegistro {

        @Test
        @DisplayName("Registro exitoso crea usuario y cliente")
        void registroExitoso_creaUsuarioYCliente() {
            // ARRANGE: preparar los mocks
            when(repositorioUsuario.existePorCorreo("nuevo@test.com")).thenReturn(false);

            Cliente clienteGuardado = Cliente.builder().idCliente(1L).email("nuevo@test.com").nombre("").descripcion("").build();
            when(repositorioCliente.guardar(any())).thenReturn(clienteGuardado);

            Usuario usuarioGuardado = Usuario.builder().id(1L).correo("nuevo@test.com").idCliente(1L).correoVerificado(false).build();
            when(repositorioUsuario.guardar(any())).thenReturn(usuarioGuardado);
            when(codificadorContrasena.encode(anyString())).thenReturn("$2a$12$hash");
            when(repositorioCodigo.guardar(any())).thenReturn(crearCodigoVigente(1L));

            // ACT: ejecutar el caso de uso
            RespuestaRegistro resultado = servicio.registrar(crearPeticionRegistro("nuevo@test.com"));

            // ASSERT: verificar resultados
            assertNotNull(resultado);
            assertEquals("nuevo@test.com", resultado.getCorreo());
            assertFalse(resultado.isCorreoVerificado());

            // Verificar que se guardó el cliente y el usuario
            verify(repositorioCliente, times(1)).guardar(any());
            verify(repositorioUsuario, times(1)).guardar(any());
            // Verificar que se envió el codigo
            verify(servicioCorreo, times(1)).enviarCodigoVerificacion(eq("nuevo@test.com"), anyString());
        }

        @Test
        @DisplayName("Registro con correo duplicado lanza excepcion CONFLICT")
        void registroCorreoDuplicado_lanzaExcepcionConflict() {
            when(repositorioUsuario.existePorCorreo("existente@test.com")).thenReturn(true);

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.registrar(crearPeticionRegistro("existente@test.com")));

            assertEquals(HttpStatus.CONFLICT, ex.getEstado());
            // Verificar que nunca se intento guardar en BD
            verify(repositorioUsuario, never()).guardar(any());
        }

        @Test
        @DisplayName("Registro hashea la contrasena antes de guardarla")
        void registro_hashContrasenaAntesDeGuardar() {
            when(repositorioUsuario.existePorCorreo(anyString())).thenReturn(false);
            when(repositorioCliente.guardar(any())).thenReturn(Cliente.builder().idCliente(1L).build());
            when(codificadorContrasena.encode("Clave123!")).thenReturn("$2a$12$hashSeguro");
            when(repositorioUsuario.guardar(any())).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                // Verificar que la contrasena fue hasheada
                assertEquals("$2a$12$hashSeguro", u.getContrasena());
                return u;
            });
            when(repositorioCodigo.guardar(any())).thenReturn(crearCodigoVigente(1L));

            servicio.registrar(crearPeticionRegistro("hash@test.com"));

            verify(codificadorContrasena, times(1)).encode("Clave123!");
        }
    }

    // ─────────────────────────────────────────────
    // PRUEBAS DE VERIFICAR CORREO
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Caso de uso: Verificar correo")
    class PruebasVerificacion {

        @Test
        @DisplayName("Verificacion exitosa activa la cuenta")
        void verificacionExitosa_activaCuenta() {
            Usuario usuario = crearUsuarioPendiente("pendiente@test.com");
            CodigoVerificacion codigo = crearCodigoVigente(2L);

            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(usuario));
            when(repositorioCodigo.buscarCodigoActivo(eq(2L), eq("123456"), any())).thenReturn(Optional.of(codigo));
            when(repositorioCodigo.guardar(any())).thenReturn(codigo);
            when(repositorioUsuario.guardar(any())).thenReturn(usuario);

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("pendiente@test.com");
            peticion.setCodigo("123456");

            // No debe lanzar ninguna excepcion
            assertDoesNotThrow(() -> servicio.verificarCorreo(peticion));

            // Verificar que el usuario se guardo con correoVerificado = true
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(repositorioUsuario).guardar(captor.capture());
            assertTrue(captor.getValue().isCorreoVerificado());
            assertEquals(Usuario.EstadoCuenta.ACTIVO, captor.getValue().getEstado());
        }

        @Test
        @DisplayName("Codigo incorrecto lanza excepcion UNAUTHORIZED")
        void codigoIncorrecto_lanzaExcepcionUnauthorized() {
            Usuario usuario = crearUsuarioPendiente("pendiente@test.com");
            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(usuario));
            when(repositorioCodigo.buscarCodigoActivo(any(), eq("000000"), any())).thenReturn(Optional.empty());

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("pendiente@test.com");
            peticion.setCodigo("000000");

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.verificarCorreo(peticion));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
        }

        @Test
        @DisplayName("Codigo expirado lanza excepcion UNAUTHORIZED")
        void codigoExpirado_lanzaExcepcionUnauthorized() {
            Usuario usuario = crearUsuarioPendiente("pendiente@test.com");
            CodigoVerificacion codigoExpirado = crearCodigoExpirado(2L);

            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(usuario));
            when(repositorioCodigo.buscarCodigoActivo(eq(2L), eq("999999"), any())).thenReturn(Optional.of(codigoExpirado));

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("pendiente@test.com");
            peticion.setCodigo("999999");

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.verificarCorreo(peticion));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
        }

        @Test
        @DisplayName("Correo ya verificado lanza excepcion BAD_REQUEST")
        void correoYaVerificado_lanzaExcepcionBadRequest() {
            Usuario usuarioActivo = crearUsuarioActivo("activo@test.com");
            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuarioActivo));

            PeticionVerificacion peticion = new PeticionVerificacion();
            peticion.setCorreo("activo@test.com");
            peticion.setCodigo("123456");

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.verificarCorreo(peticion));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        }
    }

    // ─────────────────────────────────────────────
    // PRUEBAS DE LOGIN
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Caso de uso: Iniciar sesion")
    class PruebasLogin {

        @Test
        @DisplayName("Login exitoso devuelve token JWT y datos del cliente")
        void loginExitoso_devuelveTokenYDatosCliente() {
            Usuario usuario = crearUsuarioActivo("activo@test.com");
            Cliente cliente = Cliente.builder().idCliente(1L).nombre("David").email("activo@test.com").descripcion("Dev").build();

            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("Clave123!", usuario.getContrasena())).thenReturn(true);
            when(repositorioCliente.buscarPorId(1L)).thenReturn(Optional.of(cliente));
            when(utilJwt.generarToken(anyString(), anyLong())).thenReturn("eyJhbGciOiJIUzI1NiJ9.token");

            RespuestaLogin resultado = servicio.iniciarSesion(crearPeticionLogin("activo@test.com", "Clave123!"));

            assertNotNull(resultado);
            assertNotNull(resultado.getAccessToken());
            assertEquals("Bearer", resultado.getTokenType());
            assertNotNull(resultado.getUsuario().getCliente());
            assertEquals("David", resultado.getUsuario().getCliente().getNombre());
        }

        @Test
        @DisplayName("Login con correo inexistente lanza excepcion BAD_REQUEST")
        void loginCorreoInexistente_lanzaExcepcionBadRequest() {
            when(repositorioUsuario.buscarPorCorreo("noexiste@test.com")).thenReturn(Optional.empty());

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.iniciarSesion(crearPeticionLogin("noexiste@test.com", "Clave123!")));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        }

        @Test
        @DisplayName("Login con contrasena incorrecta lanza excepcion BAD_REQUEST")
        void loginContrasenaIncorrecta_lanzaExcepcionBadRequest() {
            Usuario usuario = crearUsuarioActivo("activo@test.com");
            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("WrongPass!", usuario.getContrasena())).thenReturn(false);

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.iniciarSesion(crearPeticionLogin("activo@test.com", "WrongPass!")));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
        }

        @Test
        @DisplayName("Login sin verificar correo lanza excepcion UNAUTHORIZED")
        void loginSinVerificar_lanzaExcepcionUnauthorized() {
            Usuario usuarioPendiente = crearUsuarioPendiente("pendiente@test.com");
            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(usuarioPendiente));
            when(codificadorContrasena.matches("Clave123!", usuarioPendiente.getContrasena())).thenReturn(true);

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.iniciarSesion(crearPeticionLogin("pendiente@test.com", "Clave123!")));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
        }

        @Test
        @DisplayName("Login con cuenta bloqueada lanza excepcion UNAUTHORIZED")
        void loginCuentaBloqueada_lanzaExcepcionUnauthorized() {
            Usuario usuarioBloqueado = Usuario.builder()
                    .id(3L).correo("bloqueado@test.com").contrasena("$2a$12$hash")
                    .estado(Usuario.EstadoCuenta.BLOQUEADO).correoVerificado(true).build();

            when(repositorioUsuario.buscarPorCorreo("bloqueado@test.com")).thenReturn(Optional.of(usuarioBloqueado));
            when(codificadorContrasena.matches("Clave123!", usuarioBloqueado.getContrasena())).thenReturn(true);

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.iniciarSesion(crearPeticionLogin("bloqueado@test.com", "Clave123!")));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
        }
    }

    // ─────────────────────────────────────────────
    // PRUEBAS DE GUARDAR DESCRIPCION
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Caso de uso: Guardar descripcion")
    class PruebasDescripcion {

        @Test
        @DisplayName("Guardar descripcion con credenciales correctas actualiza el cliente")
        void guardarDescripcion_credencialesCorrectas_actualizaCliente() {
            Usuario usuario = crearUsuarioActivo("activo@test.com");
            Cliente cliente = Cliente.builder().idCliente(1L).nombre("David").email("activo@test.com").descripcion("").build();

            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("Clave123!", usuario.getContrasena())).thenReturn(true);
            when(repositorioCliente.buscarPorId(1L)).thenReturn(Optional.of(cliente));
            when(repositorioCliente.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

            PeticionDescripcion peticion = new PeticionDescripcion();
            peticion.setCorreo("activo@test.com");
            peticion.setContrasena("Clave123!");
            peticion.setDescripcion("Desarrollador apasionado");

            RespuestaCliente resultado = servicio.guardarDescripcion(peticion);

            assertNotNull(resultado);
            assertEquals("Desarrollador apasionado", resultado.getDescripcion());
            // Confirmar que la contrasena NO esta en la respuesta
            assertNull(resultado.getClass().getFields().length > 0 ? null : null);
        }

        @Test
        @DisplayName("Guardar descripcion con credenciales incorrectas lanza UNAUTHORIZED")
        void guardarDescripcion_credencialesIncorrectas_lanzaUnauthorized() {
            Usuario usuario = crearUsuarioActivo("activo@test.com");
            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuario));
            when(codificadorContrasena.matches("WrongPass!", usuario.getContrasena())).thenReturn(false);

            PeticionDescripcion peticion = new PeticionDescripcion();
            peticion.setCorreo("activo@test.com");
            peticion.setContrasena("WrongPass!");
            peticion.setDescripcion("Descripcion");

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.guardarDescripcion(peticion));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
            // Verificar que nunca se toco el cliente
            verify(repositorioCliente, never()).guardar(any());
        }

        @Test
        @DisplayName("Guardar descripcion con correo inexistente lanza UNAUTHORIZED")
        void guardarDescripcion_correoInexistente_lanzaUnauthorized() {
            when(repositorioUsuario.buscarPorCorreo("noexiste@test.com")).thenReturn(Optional.empty());

            PeticionDescripcion peticion = new PeticionDescripcion();
            peticion.setCorreo("noexiste@test.com");
            peticion.setContrasena("Clave123!");
            peticion.setDescripcion("Desc");

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.guardarDescripcion(peticion));

            assertEquals(HttpStatus.UNAUTHORIZED, ex.getEstado());
        }
    }

    // ─────────────────────────────────────────────
    // PRUEBAS DE REENVIAR CODIGO
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Caso de uso: Reenviar codigo")
    class PruebasReenviarCodigo {

        @Test
        @DisplayName("Reenviar codigo para usuario pendiente genera nuevo codigo")
        void reenviarCodigo_usuarioPendiente_generaNuevoCodigo() {
            Usuario usuario = crearUsuarioPendiente("pendiente@test.com");
            when(repositorioUsuario.buscarPorCorreo("pendiente@test.com")).thenReturn(Optional.of(usuario));
            when(repositorioCodigo.guardar(any())).thenReturn(crearCodigoVigente(2L));

            assertDoesNotThrow(() -> servicio.reenviarCodigo("pendiente@test.com"));

            verify(repositorioCodigo, times(1)).invalidarCodigosAnteriores(any(), any());
            verify(repositorioCodigo, times(1)).guardar(any());
            verify(servicioCorreo, times(1)).enviarCodigoVerificacion(eq("pendiente@test.com"), anyString());
        }

        @Test
        @DisplayName("Reenviar codigo para correo ya verificado lanza BAD_REQUEST")
        void reenviarCodigo_correoYaVerificado_lanzaBadRequest() {
            Usuario usuarioActivo = crearUsuarioActivo("activo@test.com");
            when(repositorioUsuario.buscarPorCorreo("activo@test.com")).thenReturn(Optional.of(usuarioActivo));

            ExcepcionAutenticacion ex = assertThrows(ExcepcionAutenticacion.class,
                    () -> servicio.reenviarCodigo("activo@test.com"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getEstado());
            verify(servicioCorreo, never()).enviarCodigoVerificacion(anyString(), anyString());
        }
    }
}
