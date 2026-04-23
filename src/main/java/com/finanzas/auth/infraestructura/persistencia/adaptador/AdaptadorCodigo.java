package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.compartido.excepcion.ExcepcionAutenticacion;
import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCodigo;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCodigoVerificacion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaCodigo;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdaptadorCodigo implements PuertoRepositorioCodigo {

    private final RepositorioJpaCodigo repositorioJpa;
    private final RepositorioJpaUsuario repositorioJpaUsuario;

    @Override
    public CodigoVerificacion guardar(CodigoVerificacion codigo) {
        EntidadUsuario entidadUsuario = repositorioJpaUsuario
                .findById(codigo.getUsuarioId())
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "Usuario no encontrado al guardar codigo", HttpStatus.NOT_FOUND));

        EntidadCodigoVerificacion entidad = ConvertidorCodigo.aEntidad(codigo, entidadUsuario);
        EntidadCodigoVerificacion guardado = repositorioJpa.save(entidad);
        return ConvertidorCodigo.aDominio(guardado);
    }

    @Override
    public Optional<CodigoVerificacion> buscarCodigoActivo(
            Long usuarioId, String codigo, CodigoVerificacion.TipoCodigo tipo) {

        return repositorioJpaUsuario.findById(usuarioId)
                .flatMap(entidadUsuario -> {
                    EntidadCodigoVerificacion.TipoCodigo tipoEntidad =
                            EntidadCodigoVerificacion.TipoCodigo.valueOf(tipo.name());
                    return repositorioJpa.findByUsuarioAndCodigoAndTipoAndUsadoFalse(
                            entidadUsuario, codigo, tipoEntidad);
                })
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
