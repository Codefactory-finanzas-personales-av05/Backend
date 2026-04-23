package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCodigoVerificacion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;

/*
 * Convierte entre CodigoVerificacion (dominio) y EntidadCodigoVerificacion (JPA).
 *
 * El dominio usa: fechaExpiracion, fechaCreacion
 * La entidad usa: expiraEn, creadoEn
 * Este convertidor hace la traduccion entre los dos.
 */
public class ConvertidorCodigo {

    // EntidadCodigoVerificacion → CodigoVerificacion (dominio)
    public static CodigoVerificacion aDominio(EntidadCodigoVerificacion entidad) {
        if (entidad == null) return null;

        return CodigoVerificacion.builder()
                .id(entidad.getIdCodigo())
                .usuarioId(entidad.getUsuario().getIdUsuario())
                .codigo(entidad.getCodigo())
                .tipo(CodigoVerificacion.TipoCodigo.valueOf(entidad.getTipo().name()))
                .fechaExpiracion(entidad.getExpiraEn())
                .usado(entidad.isUsado())
                .fechaCreacion(entidad.getCreadoEn())
                .build();
    }

    // CodigoVerificacion (dominio) → EntidadCodigoVerificacion
    public static EntidadCodigoVerificacion aEntidad(
            CodigoVerificacion codigo, EntidadUsuario entidadUsuario) {
        if (codigo == null) return null;

        return EntidadCodigoVerificacion.builder()
                .idCodigo(codigo.getId())
                .usuario(entidadUsuario)
                .codigo(codigo.getCodigo())
                .tipo(EntidadCodigoVerificacion.TipoCodigo.valueOf(codigo.getTipo().name()))
                .expiraEn(codigo.getFechaExpiracion())
                .usado(codigo.isUsado())
                .creadoEn(codigo.getFechaCreacion())
                .build();
    }

    private ConvertidorCodigo() {}
}
