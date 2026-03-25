package com.finanzas.auth.infraestructura.persistencia.repositorio;

import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repositorio Spring Data JPA - Spring genera la implementacion automaticamente
public interface RepositorioJpaUsuario extends JpaRepository<EntidadUsuario, Long> {

    Optional<EntidadUsuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
