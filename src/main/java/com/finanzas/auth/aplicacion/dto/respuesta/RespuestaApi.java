package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.Builder;
import lombok.Data;

/*
 * Envoltorio generico para todas las respuestas de la API.
 * Siempre devuelve: status, mensaje y data (el contenido real).
 * Asi el frontend siempre recibe el mismo formato sin importar el endpoint.
 */
@Data
@Builder
public class RespuestaApi<T> {

    private int status;
    private String mensaje;
    private T data;

    // Respuesta exitosa normal
    public static <T> RespuestaApi<T> exito(String mensaje, T data) {
        return RespuestaApi.<T>builder()
                .status(200)
                .mensaje(mensaje)
                .data(data)
                .build();
    }

    // Respuesta cuando se crea algo nuevo
    public static <T> RespuestaApi<T> creado(String mensaje, T data) {
        return RespuestaApi.<T>builder()
                .status(201)
                .mensaje(mensaje)
                .data(data)
                .build();
    }
}
