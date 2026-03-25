package com.finanzas.auth.dominio.puertos.salida;

import com.finanzas.auth.dominio.modelo.Usuario;
import java.util.Optional;

/*
 * Puerto de SALIDA (driven port) para usuarios.
 * El dominio define lo que necesita de la base de datos
 * pero no le importa si es MySQL, H2, MongoDB u otra cosa.
 * La implementacion real va en la capa de infraestructura.
 */
public interface PuertoRepositorioUsuario {

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);

    Usuario guardar(Usuario usuario);
}
