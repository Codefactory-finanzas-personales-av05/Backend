package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.Usuario;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioUsuario;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
 * Adaptador de SALIDA (Driven Adapter) para usuarios.
 * Implementa el puerto de salida usando Spring Data JPA.
 * Si manana cambiamos a MongoDB, solo cambiamos este archivo.
 */
@Component
@RequiredArgsConstructor
public class AdaptadorUsuario implements PuertoRepositorioUsuario {

    private final RepositorioJpaUsuario repositorioJpa;

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return repositorioJpa.findByCorreo(correo)
                .map(ConvertidorUsuario::aDominio);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repositorioJpa.existsByCorreo(correo);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        EntidadUsuario entidad = ConvertidorUsuario.aEntidad(usuario);
        EntidadUsuario guardado = repositorioJpa.save(entidad);
        return ConvertidorUsuario.aDominio(guardado);
    }
}
