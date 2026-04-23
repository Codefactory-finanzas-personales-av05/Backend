package com.finanzas.auth.infraestructura.persistencia.repositorio;

import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioJpaCategoria extends JpaRepository<EntidadCategoria, Long> {

    // SELECT * FROM categorias WHERE id_cliente = ?
    List<EntidadCategoria> findByIdCliente(Long idCliente);

    // SELECT * FROM categorias WHERE id_cliente = ? AND tipo = ?
    List<EntidadCategoria> findByIdClienteAndTipo(Long idCliente, EntidadCategoria.TipoCategoria tipo);
}
