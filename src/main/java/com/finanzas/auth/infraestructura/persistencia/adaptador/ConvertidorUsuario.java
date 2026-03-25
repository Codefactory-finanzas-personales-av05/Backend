package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.Usuario;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;

/*
 * Convierte entre el modelo de dominio y la entidad JPA.
 * Esto es necesario para mantener el dominio limpio sin anotaciones de Spring.
 *
 * Cada vez que sacamos datos de la BD los convertimos a dominio,
 * y cuando vamos a guardar convertimos de dominio a entidad.
 */
public class ConvertidorUsuario {

    // De entidad JPA -> modelo de dominio
    public static Usuario aDominio(EntidadUsuario entidad) {
        if (entidad == null) return null;

        return Usuario.builder()
                .id(entidad.getId())
                .correo(entidad.getCorreo())
                .contrasena(entidad.getContrasena())
                .idCliente(entidad.getIdCliente())
                .estado(Usuario.EstadoCuenta.valueOf(entidad.getEstado().name()))
                .correoVerificado(entidad.isCorreoVerificado())
                .fechaCreacion(entidad.getFechaCreacion())
                .fechaActualizacion(entidad.getFechaActualizacion())
                .build();
    }

    // De modelo de dominio -> entidad JPA
    public static EntidadUsuario aEntidad(Usuario usuario) {
        if (usuario == null) return null;

        return EntidadUsuario.builder()
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .contrasena(usuario.getContrasena())
                .idCliente(usuario.getIdCliente())
                .estado(EntidadUsuario.EstadoCuenta.valueOf(usuario.getEstado().name()))
                .correoVerificado(usuario.isCorreoVerificado())
                .build();
    }

    private ConvertidorUsuario() {}
}
