package com.finanzas.auth.dominio;

import com.finanzas.auth.dominio.modelo.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias del modelo de dominio Usuario")
class UsuarioTest {

    // ----------------------------------------------------------------- Test 15
    @Test
    @DisplayName("El estado por defecto de un usuario nuevo es PENDIENTE_VERIFICACION")
    void estadoPorDefecto_debeSer_PendienteVerificacion() {
        // Arrange & Act
        Usuario usuario = Usuario.builder()
                .correo("nuevo@test.com")
                .contrasena("$2a$12$hashDeContrasena")
                .build();

        // Assert
        assertEquals(Usuario.EstadoCuenta.PENDIENTE_VERIFICACION, usuario.getEstado());
    }

    // ----------------------------------------------------------------- Test 16
    @Test
    @DisplayName("El flag correoVerificado es false por defecto al crear un usuario")
    void correoVerificado_debeSer_FalsoPorDefecto() {
        // Arrange & Act
        Usuario usuario = Usuario.builder()
                .correo("nuevo@test.com")
                .build();

        // Assert
        assertFalse(usuario.isCorreoVerificado());
    }

    // ----------------------------------------------------------------- Test 17
    @Test
    @DisplayName("El builder construye un Usuario con todos los campos asignados correctamente")
    void builder_debeConstruirUsuario_ConTodosLosCampos() {
        // Arrange
        LocalDateTime ahora = LocalDateTime.now();

        // Act
        Usuario usuario = Usuario.builder()
                .id(5L)
                .correo("admin@finanzas.com")
                .contrasena("$2a$12$hashedPass")
                .idCliente(3L)
                .estado(Usuario.EstadoCuenta.ACTIVO)
                .correoVerificado(true)
                .creadoEn(ahora)
                .actualizadoEn(ahora)
                .build();

        // Assert
        assertAll("Todos los campos del usuario deben coincidir",
                () -> assertEquals(5L, usuario.getId()),
                () -> assertEquals("admin@finanzas.com", usuario.getCorreo()),
                () -> assertEquals(3L, usuario.getIdCliente()),
                () -> assertEquals(Usuario.EstadoCuenta.ACTIVO, usuario.getEstado()),
                () -> assertTrue(usuario.isCorreoVerificado()),
                () -> assertEquals(ahora, usuario.getCreadoEn())
        );
    }
}
