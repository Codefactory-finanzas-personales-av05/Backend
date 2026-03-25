package com.finanzas.auth.infraestructura.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

/*
 * Entidad JPA para la tabla clientes.
 * Guarda el perfil del usuario: nombre, email y descripcion.
 */
@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(length = 150)
    private String nombre;

    @Column(length = 150)
    private String email;

    // La descripcion puede ser larga, por eso usamos TEXT
    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
