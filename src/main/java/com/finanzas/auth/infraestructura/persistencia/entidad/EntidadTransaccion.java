package com.finanzas.auth.infraestructura.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Entidad JPA para la tabla "transacciones".
 * Sincronizada con el script PostgreSQL del compañero de BD.
 */
@Entity
@Table(name = "transacciones")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadTransaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Long idTransaccion;

    @Column(nullable = false, length = 150)
    private String nombre;

    // NUMERIC(15,2) en PostgreSQL — BigDecimal en Java para precision exacta
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    // Fecha en que ocurrio el movimiento (la que ingresa el usuario)
    @Column(name = "movimiento_en", nullable = false)
    private LocalDateTime movimientoEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransaccion tipo;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    // Relacion con categoria para obtener nombre e icono sin join extra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private EntidadCategoria categoria;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public enum TipoTransaccion {
        INGRESO,
        GASTO
    }
}
