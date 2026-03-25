package com.finanzas.auth.infraestructura.web;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionDescripcion;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionLogin;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionRegistro;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionVerificacion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaApi;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCliente;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaLogin;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaRegistro;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoAutenticacion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * Adaptador de ENTRADA - traduce requests HTTP a llamadas al caso de uso.
 * Solo conoce la interfaz CasoDeUsoAutenticacion, no la implementacion.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ControladorAutenticacion {

    private final CasoDeUsoAutenticacion casoDeUsoAuth;

    /*
     * POST /api/auth/registro
     * Historia 1: recibe correo y contrasena
     * Correcto: 200 | Error validacion: 400 | Correo duplicado: 409
     */
    @PostMapping("/registro")
    public ResponseEntity<RespuestaApi<RespuestaRegistro>> registro(
            @Valid @RequestBody PeticionRegistro peticion) {

        RespuestaRegistro datos = casoDeUsoAuth.registrar(peticion);

        return ResponseEntity.ok(
                RespuestaApi.exito("Usuario registrado. Revisa tu correo para verificar tu cuenta.", datos));
    }

    /*
     * POST /api/auth/verificar
     * Historia 2: valida el codigo de correo electronico
     * Correcto: 201 | Incorrecto/expirado: 401
     */
    @PostMapping("/verificar")
    public ResponseEntity<RespuestaApi<Void>> verificarCorreo(
            @Valid @RequestBody PeticionVerificacion peticion) {

        casoDeUsoAuth.verificarCorreo(peticion);

        // Segun el requerimiento: devuelve 201 cuando es correcto
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RespuestaApi.<Void>builder()
                        .status(201)
                        .mensaje("Correo verificado exitosamente. Ya puedes iniciar sesion!")
                        .data(null)
                        .build());
    }

    /*
     * POST /api/auth/login
     * Historia 1: confirma correo y contrasena
     * Correcto: 200 con datos del cliente | Incorrecto: 400 con mensaje
     */
    @PostMapping("/login")
    public ResponseEntity<RespuestaApi<RespuestaLogin>> login(
            @Valid @RequestBody PeticionLogin peticion) {

        RespuestaLogin datos = casoDeUsoAuth.iniciarSesion(peticion);

        return ResponseEntity.ok(
                RespuestaApi.exito("Inicio de sesion exitoso. Bienvenido!", datos));
    }

    /*
     * POST /api/auth/descripcion
     * Historia 3: recibe descripcion del usuario para guardar en la BBDD
     * Credenciales incorrectas: 401 | Correctas: 200 con informacion del cliente
     * La contrasena NUNCA se devuelve en la respuesta
     */
    @PostMapping("/descripcion")
    public ResponseEntity<RespuestaApi<RespuestaCliente>> guardarDescripcion(
            @Valid @RequestBody PeticionDescripcion peticion) {

        RespuestaCliente datos = casoDeUsoAuth.guardarDescripcion(peticion);

        return ResponseEntity.ok(
                RespuestaApi.exito("Descripcion guardada correctamente.", datos));
    }

    /*
     * POST /api/auth/reenviar-codigo?correo=xxx@yyy.com
     * Reenviar el codigo si expiro o no llego al correo
     */
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<RespuestaApi<Void>> reenviarCodigo(
            @RequestParam @NotBlank @Email String correo) {

        casoDeUsoAuth.reenviarCodigo(correo);

        return ResponseEntity.ok(RespuestaApi.<Void>builder()
                .status(200)
                .mensaje("Codigo de verificacion reenviado a " + correo)
                .data(null)
                .build());
    }
}
