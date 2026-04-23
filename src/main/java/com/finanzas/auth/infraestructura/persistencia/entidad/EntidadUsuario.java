package com.finanzas.auth.infraestructura.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
 * Entidad JPA sincronizada con el script PostgreSQL del compañero de BD.
 *
 * Cambios respecto a la version anterior:
 * - "id"                  → "id_usuario"
 * - "fecha_creacion"      → "creado_en"
 * - "fecha_actualizacion" → "actualizado_en"
 * - Se mantiene "correo_verificado" (acordado con el compañero de BD)
 * - Estado usa PENDIENTE_VERIFICACION (acordado con el compañero)
 */
@Entity
@Table(name = "usuarios",
    uniqueConstraints = @UniqueConstraint(columnNames = "correo", name = "uk_usuarios_correo"))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    // FK hacia clientes — UNIQUE porque es relacion 1:1
    @Column(name = "id_cliente", unique = true)
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCuenta estado;

    // Campo acordado con el compañero de BD para el flujo de verificacion
    @Column(name = "correo_verificado", nullable = false)
    @Builder.Default
    private boolean correoVerificado = false;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // Acordado con el compañero de BD: PENDIENTE_VERIFICACION en vez de PENDIENTE
    public enum EstadoCuenta {
        PENDIENTE_VERIFICACION,
        ACTIVO,
        INACTIVO,
        BLOQUEADO
    }
}
