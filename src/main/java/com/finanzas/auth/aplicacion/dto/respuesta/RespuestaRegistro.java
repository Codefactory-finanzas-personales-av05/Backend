package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.Builder;
import lombok.Data;

// Lo que devuelve el endpoint de registro
@Data
@Builder
public class RespuestaRegistro {
    private Long id;
    private String correo;
    private String mensaje;
    private boolean correoVerificado;
}
