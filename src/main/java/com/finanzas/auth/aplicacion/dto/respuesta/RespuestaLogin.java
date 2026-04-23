package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.Builder;
import lombok.Data;

// Lo que devuelve el endpoint de login: token JWT + datos del usuario + datos del cliente
@Data
@Builder
public class RespuestaLogin {

    private String accessToken;
    private String tokenType;
    private Long expiraEn;
    private DatosUsuario usuario;

    @Data
    @Builder
    public static class DatosUsuario {
        private Long id;
        private String correo;
        private String estado;
        private DatosCliente cliente;
    }

    @Data
    @Builder
    public static class DatosCliente {
        private Long idCliente;
        private String nombre;
        private String correoContacto;
        private String imagenPerfil;
        private String descripcion;
    }
}
