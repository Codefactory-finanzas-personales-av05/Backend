package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.Builder;
import lombok.Data;

/*
 * Lo que devuelve el endpoint de guardar descripcion.
 * Devuelve todos los datos del cliente EXCEPTO la contrasena.
 */
@Data
@Builder
public class RespuestaCliente {

    private Long idCliente;
    private String nombre;
    private String email;
    private String descripcion;

    // Datos del usuario asociado (sin contrasena)
    private Long idUsuario;
    private String correo;
    private String estado;
}
