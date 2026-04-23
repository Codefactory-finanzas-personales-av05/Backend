package com.finanzas.auth.infraestructura.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
 * Entidad JPA para la tabla "categorias".
 * Sincronizada con el script PostgreSQL del compañero de BD.
 */
@Entity
@Table(name = "categorias")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String icono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCategoria tipo;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public enum TipoCategoria {
        INGRESO,
        GASTO
    }
}
