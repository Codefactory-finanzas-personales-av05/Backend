package com.finanzas.auth.aplicacion.casosdeuso;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionTransaccion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCategoria;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaHistorial;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaItemHistorial;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaTransaccion;
import com.finanzas.auth.compartido.excepcion.ExcepcionAutenticacion;
import com.finanzas.auth.dominio.modelo.Categoria;
import com.finanzas.auth.dominio.modelo.Transaccion;
import com.finanzas.auth.dominio.modelo.Usuario;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoTransaccion;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCategoria;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioTransaccion;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioTransaccion implements CasoDeUsoTransaccion {

    private final PuertoRepositorioTransaccion repositorioTransaccion;
    private final PuertoRepositorioCategoria repositorioCategoria;
    private final PuertoRepositorioUsuario repositorioUsuario;

    // -------------------------------------------------------------------------
    // HU-03 / HU-04 — Registrar ingreso o gasto
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public RespuestaTransaccion registrar(PeticionTransaccion peticion, String correoUsuario) {
        log.info("Registrando transaccion tipo {} para usuario: {}", peticion.getTipo(), correoUsuario);

        // 1. Obtener el cliente a partir del correo del token JWT
        Usuario usuario = buscarUsuarioPorCorreo(correoUsuario);

        // 2. Validar que la categoria existe y pertenece al cliente
        //    (regla de seguridad: un usuario no puede usar categorias de otro)
        Categoria categoria = repositorioCategoria
                .buscarPorId(peticion.getIdCategoria())
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "La categoria no existe", HttpStatus.NOT_FOUND));

        if (!categoria.getIdCliente().equals(usuario.getIdCliente())) {
            throw new ExcepcionAutenticacion(
                    "No tienes permiso para usar esta categoria", HttpStatus.FORBIDDEN);
        }

        // 3. Validar que el tipo de la transaccion coincide con el tipo de la categoria
        //    No se puede registrar un INGRESO en una categoria de GASTO y viceversa
        if (!categoria.getTipo().name().equals(peticion.getTipo().name())) {
            throw new ExcepcionAutenticacion(
                    "El tipo de la transaccion no coincide con el tipo de la categoria",
                    HttpStatus.BAD_REQUEST);
        }

        // 4. Construir y guardar la transaccion
        Transaccion nuevaTransaccion = Transaccion.builder()
                .nombre(peticion.getNombre())
                .monto(peticion.getMonto())
                .movimientoEn(peticion.getMovimientoEn())
                .tipo(peticion.getTipo())
                .idCliente(usuario.getIdCliente())
                .idCategoria(categoria.getIdCategoria())
                .build();

        Transaccion guardada = repositorioTransaccion.guardar(nuevaTransaccion);
        log.info("Transaccion registrada ID: {}", guardada.getId());

        // 5. Calcular el balance actualizado para devolver al frontend
        BigDecimal totalIngresos = repositorioTransaccion
                .sumarMontosPorTipo(usuario.getIdCliente(), Transaccion.TipoTransaccion.INGRESO);
        BigDecimal totalGastos = repositorioTransaccion
                .sumarMontosPorTipo(usuario.getIdCliente(), Transaccion.TipoTransaccion.GASTO);
        BigDecimal balance = totalIngresos.subtract(totalGastos);

        return RespuestaTransaccion.builder()
                .id(guardada.getId())
                .nombre(guardada.getNombre())
                .monto(guardada.getMonto())
                .movimientoEn(guardada.getMovimientoEn())
                .tipo(guardada.getTipo().name())
                .nombreCategoria(categoria.getNombre())
                .iconoCategoria(categoria.getIcono())
                .balanceActual(balance)
                .creadoEn(guardada.getCreadoEn())
                .build();
    }

    // -------------------------------------------------------------------------
    // HU-05 — Historial paginado de transacciones
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public RespuestaHistorial obtenerHistorial(String correoUsuario, int pagina, int tamano) {
        log.info("Obteniendo historial para: {} - pagina {} tamano {}", correoUsuario, pagina, tamano);

        Usuario usuario = buscarUsuarioPorCorreo(correoUsuario);

        // Ordenado por movimiento_en DESC: mas reciente primero (requerido en HU-05)
        PageRequest pageRequest = PageRequest.of(
                pagina, tamano,
                Sort.by(Sort.Direction.DESC, "movimientoEn"));

        Page<Transaccion> paginaTransacciones = repositorioTransaccion
                .buscarPorIdCliente(usuario.getIdCliente(), pageRequest);

        List<RespuestaItemHistorial> items = paginaTransacciones.getContent()
                .stream()
                .map(t -> RespuestaItemHistorial.builder()
                        .id(t.getId())
                        .nombre(t.getNombre())
                        .monto(t.getMonto())
                        .movimientoEn(t.getMovimientoEn())
                        .tipo(t.getTipo().name())
                        .nombreCategoria(t.getNombreCategoria())
                        .iconoCategoria(t.getIconoCategoria())
                        .build())
                .collect(Collectors.toList());

        // Calcular balance para mostrar en el resumen del historial
        BigDecimal totalIngresos = repositorioTransaccion
                .sumarMontosPorTipo(usuario.getIdCliente(), Transaccion.TipoTransaccion.INGRESO);
        BigDecimal totalGastos = repositorioTransaccion
                .sumarMontosPorTipo(usuario.getIdCliente(), Transaccion.TipoTransaccion.GASTO);
        BigDecimal balance = totalIngresos.subtract(totalGastos);

        return RespuestaHistorial.builder()
                .transacciones(items)
                .paginaActual(paginaTransacciones.getNumber())
                .totalPaginas(paginaTransacciones.getTotalPages())
                .totalElementos(paginaTransacciones.getTotalElements())
                .totalIngresos(totalIngresos)
                .totalGastos(totalGastos)
                .balanceActual(balance)
                .build();
    }

    // -------------------------------------------------------------------------
    // Categorias — necesario para el formulario de nueva transaccion
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<RespuestaCategoria> obtenerCategorias(String correoUsuario) {
        Usuario usuario = buscarUsuarioPorCorreo(correoUsuario);

        return repositorioCategoria
                .buscarPorIdCliente(usuario.getIdCliente())
                .stream()
                .map(c -> RespuestaCategoria.builder()
                        .idCategoria(c.getIdCategoria())
                        .nombre(c.getNombre())
                        .icono(c.getIcono())
                        .tipo(c.getTipo().name())
                        .build())
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Helper privado
    // -------------------------------------------------------------------------
    private Usuario buscarUsuarioPorCorreo(String correo) {
        return repositorioUsuario.buscarPorCorreo(correo)
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "Usuario no encontrado", HttpStatus.UNAUTHORIZED));
    }
}
