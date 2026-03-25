package com.finanzas.auth.infraestructura.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * Entidad JPA para los codigos de verificacion.
 * La relacion con EntidadUsuario es ManyToOne: un usuario
 * puede tener varios codigos a lo largo del tiempo.
 */
@Entity
@Table(name = "codigos_verificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadCodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Llave foranea hacia la tabla usuarios
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private EntidadUsuario usuario;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCodigo tipo;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    @Builder.Default
    private boolean usado = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum TipoCodigo {
        VERIFICACION_CORREO,
        RECUPERACION_CONTRASENA,
        DOS_FACTORES
    }
}
