package com.finanzas.auth.infraestructura.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * Entidad JPA sincronizada con el script PostgreSQL del compañero de BD.
 *
 * Cambios respecto a la version anterior:
 * - "id"               → "id_codigo"
 * - "fecha_expiracion" → "expira_en"
 * - "fecha_creacion"   → "creado_en"
 */
@Entity
@Table(name = "codigos_verificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntidadCodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codigo")
    private Long idCodigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private EntidadUsuario usuario;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCodigo tipo;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "usado", nullable = false)
    @Builder.Default
    private boolean usado = false;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    public enum TipoCodigo {
        VERIFICACION_CORREO,
        RECUPERACION_CONTRASENA,
        DOS_FACTORES
    }
}
