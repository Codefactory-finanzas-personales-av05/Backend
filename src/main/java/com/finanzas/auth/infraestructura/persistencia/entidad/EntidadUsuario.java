package com.finanzas.auth.infraestructura.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
 * Entidad JPA del usuario.
 */
@Entity
@Table(
    name = "usuarios",
    uniqueConstraints = @UniqueConstraint(columnNames = "correo", name = "uk_correo_usuario")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntidadUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    // Llave foranea hacia la tabla clientes
    @Column(name = "id_cliente")
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE_VERIFICACION;

    @Column(name = "correo_verificado", nullable = false)
    @Builder.Default
    private boolean correoVerificado = false;

    @CreatedDate
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum EstadoCuenta {
        PENDIENTE_VERIFICACION,
        ACTIVO,
        INACTIVO,
        BLOQUEADO
    }
}
