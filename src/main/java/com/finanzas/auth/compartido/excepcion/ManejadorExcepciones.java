package com.finanzas.auth.compartido.excepcion;

import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
 * Manejador global de excepciones.
 * Centraliza todos los errores para que el controlador quede limpio.
 * Cualquier excepcion no manejada pasa por aqui antes de llegar al cliente.
 */
@RestControllerAdvice
@Slf4j
public class ManejadorExcepciones {

    /*
     * Errores de validacion (@Valid en los DTOs).
     * Por ejemplo: correo mal formado, contrasena muy corta, etc.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaApi<Map<String, String>>> manejarValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
            errores.put(campo, error.getDefaultMessage());
        });

        RespuestaApi<Map<String, String>> respuesta = RespuestaApi.<Map<String, String>>builder()
                .status(400)
                .mensaje("Error de validacion en los datos enviados")
                .data(errores)
                .build();

        return ResponseEntity.badRequest().body(respuesta);
    }

    /*
     * Errores de negocio: correo ya registrado, codigo incorrecto, etc.
     */
    @ExceptionHandler(ExcepcionAutenticacion.class)
    public ResponseEntity<RespuestaApi<Void>> manejarExcepcionAuth(ExcepcionAutenticacion ex) {
        log.warn("Error de autenticacion: {}", ex.getMessage());

        RespuestaApi<Void> respuesta = RespuestaApi.<Void>builder()
                .status(ex.getEstado().value())
                .mensaje(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(ex.getEstado()).body(respuesta);
    }

    /*
     * Cualquier error inesperado que no capturamos antes.
     * Devolvemos 500 con un mensaje generico para no exponer detalles internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaApi<Void>> manejarErrorGeneral(Exception ex) {
        log.error("Error inesperado en el servidor: {}", ex.getMessage(), ex);

        RespuestaApi<Void> respuesta = RespuestaApi.<Void>builder()
                .status(500)
                .mensaje("Ocurrio un error interno. Por favor intenta mas tarde.")
                .data(null)
                .build();

        return ResponseEntity.internalServerError().body(respuesta);
    }
}
