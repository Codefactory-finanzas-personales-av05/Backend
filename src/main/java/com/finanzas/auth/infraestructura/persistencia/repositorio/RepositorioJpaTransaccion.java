package com.finanzas.auth.infraestructura.persistencia.repositorio;

import com.finanzas.auth.infraestructura.persistencia.entidad.EntidadTransaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface RepositorioJpaTransaccion extends JpaRepository<EntidadTransaccion, Long> {

    // Historial paginado — el ordenamiento viene del Pageable (Sort.by DESC en el servicio)
    Page<EntidadTransaccion> findByIdCliente(Long idCliente, Pageable pageable);

    // Suma de montos por tipo para calcular el balance
    // COALESCE devuelve 0 cuando no hay transacciones del tipo indicado
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM EntidadTransaccion t " +
           "WHERE t.idCliente = :idCliente AND t.tipo = :tipo")
    BigDecimal sumarMontosPorTipo(Long idCliente, EntidadTransaccion.TipoTransaccion tipo);
}
