package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.Builder;
import lombok.Data;

// Lo que devuelve el endpoint /descripcion — NUNCA incluye la contrasena
@Data
@Builder
public class RespuestaCliente {

    private Long idCliente;
    private String nombre;
    private String correoContacto;
    private String imagenPerfil;
    private String descripcion;

    // Datos del usuario asociado
    private Long idUsuario;
    private String correo;
    private String estado;
}
