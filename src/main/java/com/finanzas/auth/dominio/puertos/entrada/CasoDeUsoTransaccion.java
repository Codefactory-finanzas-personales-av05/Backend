package com.finanzas.auth.dominio.puertos.entrada;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionTransaccion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCategoria;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaHistorial;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaTransaccion;

import java.util.List;

/*
 * Puerto de ENTRADA para las historias de usuario HU-03, HU-04 y HU-05.
 * El controlador solo conoce esta interfaz, no la implementacion.
 */
public interface CasoDeUsoTransaccion {

    // HU-03 y HU-04: registrar un ingreso o gasto
    // correoUsuario viene del token JWT — identifica quien hace la transaccion
    RespuestaTransaccion registrar(PeticionTransaccion peticion, String correoUsuario);

    // HU-05: historial paginado con balance
    RespuestaHistorial obtenerHistorial(String correoUsuario, int pagina, int tamano);

    // Listado de categorias del cliente (necesario para el formulario de nueva transaccion)
    List<RespuestaCategoria> obtenerCategorias(String correoUsuario);
}
