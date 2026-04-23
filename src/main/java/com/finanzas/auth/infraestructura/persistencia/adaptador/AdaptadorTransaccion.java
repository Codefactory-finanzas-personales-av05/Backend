package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.Transaccion;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioTransaccion;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCategoria;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadTransaccion;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaCategoria;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaTransaccion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AdaptadorTransaccion implements PuertoRepositorioTransaccion {

    private final RepositorioJpaTransaccion repositorioJpa;
    private final RepositorioJpaCategoria repositorioJpaCategoria;

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        EntidadCategoria entidadCategoria = repositorioJpaCategoria
                .findById(transaccion.getIdCategoria())
                .orElseThrow(() -> new RuntimeException(
                        "Categoria no encontrada: " + transaccion.getIdCategoria()));

        EntidadTransaccion entidad = EntidadTransaccion.builder()
                .idTransaccion(transaccion.getId())
                .nombre(transaccion.getNombre())
                .monto(transaccion.getMonto())
                .movimientoEn(transaccion.getMovimientoEn())
                .tipo(EntidadTransaccion.TipoTransaccion.valueOf(transaccion.getTipo().name()))
                .idCliente(transaccion.getIdCliente())
                .categoria(entidadCategoria)
                .build();

        return aDominio(repositorioJpa.save(entidad));
    }

    @Override
    public Page<Transaccion> buscarPorIdCliente(Long idCliente, Pageable pageable) {
        return repositorioJpa
                .findByIdCliente(idCliente, pageable)
                .map(this::aDominio);
    }

    @Override
    public BigDecimal sumarMontosPorTipo(Long idCliente, Transaccion.TipoTransaccion tipo) {
        EntidadTransaccion.TipoTransaccion tipoEntidad =
                EntidadTransaccion.TipoTransaccion.valueOf(tipo.name());
        BigDecimal resultado = repositorioJpa.sumarMontosPorTipo(idCliente, tipoEntidad);
        return resultado != null ? resultado : BigDecimal.ZERO;
    }

    private Transaccion aDominio(EntidadTransaccion e) {
        if (e == null) return null;
        return Transaccion.builder()
                .id(e.getIdTransaccion())
                .nombre(e.getNombre())
                .monto(e.getMonto())
                .movimientoEn(e.getMovimientoEn())
                .tipo(Transaccion.TipoTransaccion.valueOf(e.getTipo().name()))
                .idCliente(e.getIdCliente())
                .idCategoria(e.getCategoria().getIdCategoria())
                .nombreCategoria(e.getCategoria().getNombre())
                .iconoCategoria(e.getCategoria().getIcono())
                .creadoEn(e.getCreadoEn())
                .actualizadoEn(e.getActualizadoEn())
                .build();
    }
}
