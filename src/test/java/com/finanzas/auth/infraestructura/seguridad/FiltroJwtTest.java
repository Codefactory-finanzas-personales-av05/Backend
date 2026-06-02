package com.finanzas.auth.infraestructura.seguridad;

import com.finanzas.auth.compartido.utilidad.UtilJwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias del FiltroJwt")
class FiltroJwtTest {

    @Mock private UtilJwt utilJwt;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private FiltroJwt filtroJwt;

    @BeforeEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sin header Authorization deja pasar el request sin autenticar")
    void sinHeader_dejaPassar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filtroJwt.doFilterInternal(request, response, filterChain);

        // El filtro debe llamar al siguiente en la cadena
        verify(filterChain).doFilter(request, response);
        // Y no debe haber autenticacion en el contexto
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Header sin Bearer deja pasar el request sin autenticar")
    void headerSinBearer_dejaPasar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filtroJwt.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Token valido autentica al usuario en el contexto de seguridad")
    void tokenValido_autenticaUsuario() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenValido");
        when(utilJwt.esTokenValido("tokenValido")).thenReturn(true);
        when(utilJwt.extraerCorreo("tokenValido")).thenReturn("david@test.com");

        filtroJwt.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("david@test.com",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("Token invalido deja pasar sin autenticar")
    void tokenInvalido_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenInvalido");
        when(utilJwt.esTokenValido("tokenInvalido")).thenReturn(false);

        filtroJwt.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Token que lanza excepcion deja pasar sin autenticar")
    void tokenConExcepcion_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenRoto");
        when(utilJwt.esTokenValido("tokenRoto")).thenThrow(new RuntimeException("JWT invalido"));

        filtroJwt.doFilterInternal(request, response, filterChain);

        // Aunque haya excepcion, la cadena de filtros debe continuar
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
