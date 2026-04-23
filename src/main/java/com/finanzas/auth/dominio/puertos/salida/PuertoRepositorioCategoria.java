package com.finanzas.auth.dominio.puertos.salida;

import com.finanzas.auth.dominio.modelo.Categoria;
import java.util.List;
import java.util.Optional;

/*
 * Puerto de SALIDA para categorias.
 * El dominio define lo que necesita de la BD — sin saber si es PostgreSQL o H2.
 */
public interface PuertoRepositorioCategoria {

    Optional<Categoria> buscarPorId(Long idCategoria);

    // Todas las categorias del cliente (para el selector de categoria en el formulario)
    List<Categoria> buscarPorIdCliente(Long idCliente);

    // Categorias del cliente filtradas por tipo (INGRESO o GASTO)
    List<Categoria> buscarPorIdClienteYTipo(Long idCliente, Categoria.TipoCategoria tipo);

    Categoria guardar(Categoria categoria);
}
