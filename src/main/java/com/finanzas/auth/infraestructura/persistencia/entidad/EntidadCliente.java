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
 * - "email"            → "correo_contacto"
 * - Se agrego           "imagen_perfil"
 * - "fecha_creacion"   → "creado_en"
 * - "fecha_actualizacion" → "actualizado_en"
 * - nombre ahora es NOT NULL
 */
@Entity
@Table(name = "clientes")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(length = 150, nullable = false)
    private String nombre;

    @Column(name = "correo_contacto", length = 150, unique = true)
    private String correoContacto;

    @Column(name = "imagen_perfil", length = 500)
    private String imagenPerfil;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
