package com.finanzas.auth.aplicacion.dto.respuesta;

import lombok.*;

/*
 * DTO de respuesta para listar las categorias del cliente.
 * El frontend lo usa para poblar el selector de categoria
 * en el formulario de nueva transaccion.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RespuestaCategoria {

    private Long idCategoria;
    private String nombre;
    private String icono;
    private String tipo;    // "INGRESO" o "GASTO"
}
