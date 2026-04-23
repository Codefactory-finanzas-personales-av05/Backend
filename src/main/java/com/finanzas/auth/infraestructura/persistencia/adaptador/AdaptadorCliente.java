package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.Cliente;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCliente;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCliente;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdaptadorCliente implements PuertoRepositorioCliente {

    private final RepositorioJpaCliente repositorioJpa;

    @Override
    public Cliente guardar(Cliente cliente) {
        EntidadCliente entidad = aEntidad(cliente);
        EntidadCliente guardado = repositorioJpa.save(entidad);
        return aDominio(guardado);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long idCliente) {
        return repositorioJpa.findById(idCliente).map(this::aDominio);
    }

    private Cliente aDominio(EntidadCliente entidad) {
        if (entidad == null) return null;
        return Cliente.builder()
                .idCliente(entidad.getIdCliente())
                .nombre(entidad.getNombre())
                .correoContacto(entidad.getCorreoContacto())
                .imagenPerfil(entidad.getImagenPerfil())
                .descripcion(entidad.getDescripcion())
                .build();
    }

    private EntidadCliente aEntidad(Cliente cliente) {
        if (cliente == null) return null;
        return EntidadCliente.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre() != null ? cliente.getNombre() : "")
                .correoContacto(cliente.getCorreoContacto())
                .imagenPerfil(cliente.getImagenPerfil())
                .descripcion(cliente.getDescripcion())
                .build();
    }
}
