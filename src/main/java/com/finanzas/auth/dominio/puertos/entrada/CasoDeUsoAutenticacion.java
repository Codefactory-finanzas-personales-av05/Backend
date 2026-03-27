package com.finanzas.auth.dominio.puertos.entrada;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionLogin;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionRegistro;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionVerificacion;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionDescripcion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaLogin;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaRegistro;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCliente;

/*
 * 
 * Define los casos de uso que expone el dominio hacia afuera.
 *
 */
public interface CasoDeUsoAutenticacion {

    // Caso de uso 1: Registrar un nuevo usuario (devuelve 200)
    RespuestaRegistro registrar(PeticionRegistro peticion);

    // Caso de uso 2: Verificar el correo con el codigo (devuelve 201)
    void verificarCorreo(PeticionVerificacion peticion);

    // Caso de uso 3: Login - correo y contrasena (correcto: 200, incorrecto: 400)
    RespuestaLogin iniciarSesion(PeticionLogin peticion);

    // Caso de uso 4: Guardar descripcion del cliente (correcto: 200 con datos, incorrecto: 401)
    RespuestaCliente guardarDescripcion(PeticionDescripcion peticion);

    // Reenviar el codigo si expiro o no llego
    void reenviarCodigo(String correo);
}
