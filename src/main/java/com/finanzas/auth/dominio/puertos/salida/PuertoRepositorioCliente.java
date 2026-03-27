package com.finanzas.auth.dominio.puertos.salida;

import com.finanzas.auth.dominio.modelo.Cliente;
import java.util.Optional;

/*
 * Puerto de SALIDA para el repositorio de clientes.
 * 
 */
public interface PuertoRepositorioCliente {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long idCliente);
}
