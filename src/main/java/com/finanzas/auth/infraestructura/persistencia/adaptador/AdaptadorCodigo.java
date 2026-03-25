package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCodigo;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCodigoVerificacion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaCodigo;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
 * Adaptador de SALIDA para los codigos de verificacion.
 */
@Component
@RequiredArgsConstructor
public class AdaptadorCodigo implements PuertoRepositorioCodigo {

    private final RepositorioJpaCodigo repositorioJpa;
    private final RepositorioJpaUsuario repositorioJpaUsuario;

    @Override
    public CodigoVerificacion guardar(CodigoVerificacion codigo) {
        // Necesitamos la entidad usuario para la relacion JPA
        EntidadUsuario entidadUsuario = repositorioJpaUsuario
                .findById(codigo.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + codigo.getUsuarioId()));

        EntidadCodigoVerificacion entidad = ConvertidorCodigo.aEntidad(codigo, entidadUsuario);
        EntidadCodigoVerificacion guardado = repositorioJpa.save(entidad);
        return ConvertidorCodigo.aDominio(guardado);
    }

    @Override
    public Optional<CodigoVerificacion> buscarCodigoActivo(
            Long usuarioId, String codigo, CodigoVerificacion.TipoCodigo tipo) {

        EntidadUsuario entidadUsuario = repositorioJpaUsuario.findById(usuarioId)
                .orElse(null);

        if (entidadUsuario == null) return Optional.empty();

        EntidadCodigoVerificacion.TipoCodigo tipoEntidad =
                EntidadCodigoVerificacion.TipoCodigo.valueOf(tipo.name());

        return repositorioJpa
                .findByUsuarioAndCodigoAndTipoAndUsadoFalse(entidadUsuario, codigo, tipoEntidad)
                .map(ConvertidorCodigo::aDominio);
    }

    @Override
    public void invalidarCodigosAnteriores(Long usuarioId, CodigoVerificacion.TipoCodigo tipo) {
        repositorioJpaUsuario.findById(usuarioId).ifPresent(entidadUsuario -> {
            EntidadCodigoVerificacion.TipoCodigo tipoEntidad =
                    EntidadCodigoVerificacion.TipoCodigo.valueOf(tipo.name());
            repositorioJpa.invalidarCodigosAnteriores(entidadUsuario, tipoEntidad);
        });
    }
}
