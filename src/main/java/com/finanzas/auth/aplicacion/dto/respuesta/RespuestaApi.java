package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.Builder;
import lombok.Data;


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
