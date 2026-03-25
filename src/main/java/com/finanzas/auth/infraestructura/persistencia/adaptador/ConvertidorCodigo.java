package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCodigoVerificacion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;

// Convierte codigos de verificacion entre dominio y entidad JPA
public class ConvertidorCodigo {

    // De entidad JPA -> modelo de dominio
    public static CodigoVerificacion aDominio(EntidadCodigoVerificacion entidad) {
        if (entidad == null) return null;

        return CodigoVerificacion.builder()
                .id(entidad.getId())
                .usuarioId(entidad.getUsuario().getId())
                .codigo(entidad.getCodigo())
                .tipo(CodigoVerificacion.TipoCodigo.valueOf(entidad.getTipo().name()))
                .fechaExpiracion(entidad.getFechaExpiracion())
                .usado(entidad.isUsado())
                .fechaCreacion(entidad.getFechaCreacion())
                .build();
    }

    // De modelo de dominio -> entidad JPA
    public static EntidadCodigoVerificacion aEntidad(
            CodigoVerificacion codigo, EntidadUsuario entidadUsuario) {

        if (codigo == null) return null;

        return EntidadCodigoVerificacion.builder()
                .id(codigo.getId())
                .usuario(entidadUsuario)
                .codigo(codigo.getCodigo())
                .tipo(EntidadCodigoVerificacion.TipoCodigo.valueOf(codigo.getTipo().name()))
                .fechaExpiracion(codigo.getFechaExpiracion())
                .usado(codigo.isUsado())
                .fechaCreacion(codigo.getFechaCreacion())
                .build();
    }

    private ConvertidorCodigo() {}
}
