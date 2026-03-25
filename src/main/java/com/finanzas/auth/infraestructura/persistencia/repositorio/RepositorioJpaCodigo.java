package com.finanzas.auth.infraestructura.persistencia.repositorio;

import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCodigoVerificacion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RepositorioJpaCodigo extends JpaRepository<EntidadCodigoVerificacion, Long> {

    Optional<EntidadCodigoVerificacion> findByUsuarioAndCodigoAndTipoAndUsadoFalse(
            EntidadUsuario usuario,
            String codigo,
            EntidadCodigoVerificacion.TipoCodigo tipo
    );

    // Invalida todos los codigos activos anteriores del mismo tipo
    @Modifying
    @Query("UPDATE EntidadCodigoVerificacion c SET c.usado = true " +
           "WHERE c.usuario = :usuario AND c.tipo = :tipo AND c.usado = false")
    void invalidarCodigosAnteriores(
            EntidadUsuario usuario,
            EntidadCodigoVerificacion.TipoCodigo tipo
    );
}
