package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.Categoria;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCategoria;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCategoria;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaCategoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdaptadorCategoria implements PuertoRepositorioCategoria {

    private final RepositorioJpaCategoria repositorioJpa;

    @Override
    public Optional<Categoria> buscarPorId(Long idCategoria) {
        return repositorioJpa.findById(idCategoria).map(this::aDominio);
    }

    @Override
    public List<Categoria> buscarPorIdCliente(Long idCliente) {
        return repositorioJpa.findByIdCliente(idCliente)
                .stream().map(this::aDominio).collect(Collectors.toList());
    }

    @Override
    public List<Categoria> buscarPorIdClienteYTipo(Long idCliente, Categoria.TipoCategoria tipo) {
        EntidadCategoria.TipoCategoria tipoEntidad =
                EntidadCategoria.TipoCategoria.valueOf(tipo.name());
        return repositorioJpa.findByIdClienteAndTipo(idCliente, tipoEntidad)
                .stream().map(this::aDominio).collect(Collectors.toList());
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        return aDominio(repositorioJpa.save(aEntidad(categoria)));
    }

    private Categoria aDominio(EntidadCategoria e) {
        if (e == null) return null;
        return Categoria.builder()
                .idCategoria(e.getIdCategoria())
                .nombre(e.getNombre())
                .icono(e.getIcono())
                .tipo(Categoria.TipoCategoria.valueOf(e.getTipo().name()))
                .idCliente(e.getIdCliente())
                .build();
    }

    private EntidadCategoria aEntidad(Categoria c) {
        if (c == null) return null;
        return EntidadCategoria.builder()
                .idCategoria(c.getIdCategoria())
                .nombre(c.getNombre())
                .icono(c.getIcono())
                .tipo(EntidadCategoria.TipoCategoria.valueOf(c.getTipo().name()))
                .idCliente(c.getIdCliente())
                .build();
    }
}
