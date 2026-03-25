package com.finanzas.auth.dominio.modelo;

import lombok.*;

/*
 * Modelo de dominio del Cliente.
 * Guarda la informacion personal del usuario: nombre, email y descripcion.
 * Sin anotaciones de JPA - eso va en la capa de infraestructura.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    private Long idCliente;
    private String nombre;
    private String email;
    private String descripcion;  // se puede actualizar despues del registro
}
