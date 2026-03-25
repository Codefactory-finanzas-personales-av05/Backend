package com.finanzas.auth.infraestructura.persistencia.adaptador;

import com.finanzas.auth.dominio.modelo.Cliente;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCliente;
import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCliente;
import com.finanzas.auth.infraestructura.persistencia.repositorio.RepositorioJpaCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
 * Adaptador de SALIDA para clientes.
 * Implementa el puerto usando Spring Data JPA.
 */
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

    // Convierte de entidad JPA a modelo de dominio
    private Cliente aDominio(EntidadCliente entidad) {
        if (entidad == null) return null;
        return Cliente.builder()
                .idCliente(entidad.getIdCliente())
                .nombre(entidad.getNombre())
                .email(entidad.getEmail())
                .descripcion(entidad.getDescripcion())
                .build();
    }

    // Convierte de modelo de dominio a entidad JPA
    private EntidadCliente aEntidad(Cliente cliente) {
        if (cliente == null) return null;
        return EntidadCliente.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .descripcion(cliente.getDescripcion())
                .build();
    }
}
