package com.finanzas.auth.infraestructura.web;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionTransaccion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaApi;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCategoria;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaHistorial;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaTransaccion;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoTransaccion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Controlador HTTP para HU-03, HU-04 y HU-05.
 *
 * TODOS los endpoints estan protegidos con JWT.
 * El correoUsuario se extrae del token via @AuthenticationPrincipal —
 * NO se pasa en el body para evitar que un usuario acceda a datos de otro.
 *
 * Base URL: /api/transacciones
 */
@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class ControladorTransaccion {

    private final CasoDeUsoTransaccion casoDeUsoTransaccion;

    // -----------------------------------------------------------------------
    // HU-03 / HU-04 — Registrar ingreso o gasto
    // POST /api/transacciones
    // Header: Authorization: Bearer <token>
    // Body: PeticionTransaccion con tipo INGRESO o GASTO
    // -----------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<RespuestaApi<RespuestaTransaccion>> registrar(
            @Valid @RequestBody PeticionTransaccion peticion,
            @AuthenticationPrincipal String correoUsuario) {

        RespuestaTransaccion respuesta = casoDeUsoTransaccion.registrar(peticion, correoUsuario);

        return ResponseEntity.ok(RespuestaApi.<RespuestaTransaccion>builder()
                .status(200)
                .mensaje("Transaccion registrada exitosamente")
                .data(respuesta)
                .build());
    }

    // -----------------------------------------------------------------------
    // HU-05 — Historial paginado de transacciones
    // GET /api/transacciones/historial?pagina=0&tamano=10
    // Header: Authorization: Bearer <token>
    // -----------------------------------------------------------------------
    @GetMapping("/historial")
    public ResponseEntity<RespuestaApi<RespuestaHistorial>> historial(
            @AuthenticationPrincipal String correoUsuario,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        RespuestaHistorial respuesta = casoDeUsoTransaccion
                .obtenerHistorial(correoUsuario, pagina, tamano);

        return ResponseEntity.ok(RespuestaApi.<RespuestaHistorial>builder()
                .status(200)
                .mensaje("Historial obtenido exitosamente")
                .data(respuesta)
                .build());
    }

    // -----------------------------------------------------------------------
    // Categorias — necesario para el formulario de nueva transaccion
    // GET /api/transacciones/categorias
    // Header: Authorization: Bearer <token>
    // -----------------------------------------------------------------------
    @GetMapping("/categorias")
    public ResponseEntity<RespuestaApi<List<RespuestaCategoria>>> categorias(
            @AuthenticationPrincipal String correoUsuario) {

        List<RespuestaCategoria> respuesta = casoDeUsoTransaccion.obtenerCategorias(correoUsuario);

        return ResponseEntity.ok(RespuestaApi.<List<RespuestaCategoria>>builder()
                .status(200)
                .mensaje("Categorias obtenidas exitosamente")
                .data(respuesta)
                .build());
    }
}
