package com.finanzas.auth.dominio.modelo;

import lombok.*;

/*
 * 
 * Guarda la informacion personal del usuario: nombre, email y descripcion.
 * 
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
    private String descripcion;  
}
