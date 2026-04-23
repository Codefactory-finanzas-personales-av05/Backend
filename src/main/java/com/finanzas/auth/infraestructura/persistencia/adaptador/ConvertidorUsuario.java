package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.Usuario;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;

/*
 * Convierte entre Usuario (dominio) y EntidadUsuario (JPA).
 */
public class ConvertidorUsuario {

    // EntidadUsuario → Usuario (dominio)
    public static Usuario aDominio(EntidadUsuario entidad) {
        if (entidad == null) return null;

        return Usuario.builder()
                .id(entidad.getIdUsuario())
                .correo(entidad.getCorreo())
                .contrasena(entidad.getContrasena())
                .idCliente(entidad.getIdCliente())
                .estado(Usuario.EstadoCuenta.valueOf(entidad.getEstado().name()))
                .correoVerificado(entidad.isCorreoVerificado())
                .creadoEn(entidad.getCreadoEn())
                .actualizadoEn(entidad.getActualizadoEn())
                .build();
    }

    // Usuario (dominio) → EntidadUsuario
    public static EntidadUsuario aEntidad(Usuario usuario) {
        if (usuario == null) return null;

        return EntidadUsuario.builder()
                .idUsuario(usuario.getId())
                .correo(usuario.getCorreo())
                .contrasena(usuario.getContrasena())
                .idCliente(usuario.getIdCliente())
                .estado(EntidadUsuario.EstadoCuenta.valueOf(usuario.getEstado().name()))
                .correoVerificado(usuario.isCorreoVerificado())
                .build();
    }

    private ConvertidorUsuario() {}
}
