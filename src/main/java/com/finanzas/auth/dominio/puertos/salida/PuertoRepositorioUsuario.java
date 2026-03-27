package com.finanzas.auth.dominio.puertos.salida;

import com.finanzas.auth.dominio.modelo.Usuario;
import java.util.Optional;

/*
 * Puerto de SALIDA (driven port) para usuarios.
 */
public interface PuertoRepositorioUsuario {

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);

    Usuario guardar(Usuario usuario);
}
