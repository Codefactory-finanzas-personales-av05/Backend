package com.finanzas.auth.infraestructura.persistencia.repositorio;

import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadCliente;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data genera la implementacion automaticamente
public interface RepositorioJpaCliente extends JpaRepository<EntidadCliente, Long> {
}
