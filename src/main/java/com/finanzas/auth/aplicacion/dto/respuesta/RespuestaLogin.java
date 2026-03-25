package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.Builder;
import lombok.Data;

// Lo que devuelve el endpoint de login: el token JWT y datos del usuario + cliente
@Data
@Builder
public class RespuestaLogin {

    private String accessToken;
    private String tokenType;
    private Long expiraEn;  // segundos que dura el token
    private DatosUsuario usuario;

    @Data
    @Builder
    public static class DatosUsuario {
        private Long id;
        private String correo;
        private String estado;

        // Datos del cliente asociado (puede ser null si aun no tiene perfil)
        private DatosCliente cliente;
    }

    @Data
    @Builder
    public static class DatosCliente {
        private Long idCliente;
        private String nombre;
        private String email;
        private String descripcion;
    }
}
