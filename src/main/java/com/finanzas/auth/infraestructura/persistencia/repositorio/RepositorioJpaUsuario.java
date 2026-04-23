package com.finanzas.auth.infraestructura.persistencia.repositorio;

import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data genera la implementacion automaticamente a partir del nombre de los metodos
public interface RepositorioJpaUsuario extends JpaRepository<EntidadUsuario, Long> {

    // SELECT * FROM usuarios WHERE correo = ?
    Optional<EntidadUsuario> findByCorreo(String correo);

    // SELECT COUNT(*) > 0 FROM usuarios WHERE correo = ?
    boolean existsByCorreo(String correo);
}
