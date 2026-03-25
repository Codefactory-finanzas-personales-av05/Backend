package com.finanzas.auth.dominio.puertos.salida;

import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import java.util.Optional;

/*
 * Puerto de SALIDA para los codigos de verificacion.
 */
public interface PuertoRepositorioCodigo {

    CodigoVerificacion guardar(CodigoVerificacion codigo);

    Optional<CodigoVerificacion> buscarCodigoActivo(
            Long usuarioId,
            String codigo,
            CodigoVerificacion.TipoCodigo tipo
    );

    // Invalida todos los codigos anteriores del mismo tipo para no tener duplicados
    void invalidarCodigosAnteriores(Long usuarioId, CodigoVerificacion.TipoCodigo tipo);
}
