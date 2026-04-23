package com.finanzas.auth.dominio.modelo;

import lombok.*;

/*
 * Modelo de dominio de Categoria.
 * Cada cliente tiene sus propias categorias de INGRESO o GASTO.
 * Sin anotaciones JPA — el dominio no sabe de bases de datos.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Categoria {

    private Long idCategoria;
    private String nombre;
    private String icono;
    private TipoCategoria tipo;
    private Long idCliente;

    public enum TipoCategoria {
        INGRESO,
        GASTO
    }
}
