package com.finanzas.auth.dominio.modelo;

import lombok.*;

/*
 * Modelo de dominio del Cliente.
 * Sin anotaciones de JPA — el dominio no sabe de bases de datos.
 *
 * Cambios:
 * - "email" → "correoContacto"
 * - Se agrego "imagenPerfil"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    private Long idCliente;
    private String nombre;
    private String correoContacto;
    private String imagenPerfil;
    private String descripcion;
}
