package com.finanzas.auth.compartido.excepcion;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
 * Excepcion personalizada para errores de autenticacion.
 * Cargamos el HttpStatus para que el manejador global sepa
 * que codigo de respuesta devolver.
 */
@Getter
public class ExcepcionAutenticacion extends RuntimeException {

    private final HttpStatus estado;

    public ExcepcionAutenticacion(String mensaje, HttpStatus estado) {
        super(mensaje);
        this.estado = estado;
    }
}
