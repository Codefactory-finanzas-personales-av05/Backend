package com.finanzas.auth.aplicacion.casosdeuso;

import com.finanzas.auth.aplicacion.dto.peticion.PeticionDescripcion;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionLogin;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionRegistro;
import com.finanzas.auth.aplicacion.dto.peticion.PeticionVerificacion;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaCliente;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaLogin;
import com.finanzas.auth.aplicacion.dto.respuesta.RespuestaRegistro;
import com.finanzas.auth.dominio.modelo.Cliente;
import com.finanzas.auth.dominio.modelo.CodigoVerificacion;
import com.finanzas.auth.dominio.modelo.Usuario;
import com.finanzas.auth.dominio.puertos.entrada.CasoDeUsoAutenticacion;
import com.finanzas.auth.dominio.puertos.salida.PuertoCorreo;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCodigo;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioCliente;
import com.finanzas.auth.dominio.puertos.salida.PuertoRepositorioUsuario;
import com.finanzas.auth.compartido.excepcion.ExcepcionAutenticacion;
import com.finanzas.auth.compartido.utilidad.GeneradorCodigo;
import com.finanzas.auth.compartido.utilidad.UtilJwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
 * Implementacion de todos los casos de uso de autenticacion.
 * Esta clase orquesta los puertos para cumplir cada historia de usuario.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioAutenticacion implements CasoDeUsoAutenticacion {

    private final PuertoRepositorioUsuario repositorioUsuario;
    private final PuertoRepositorioCodigo repositorioCodigo;
    private final PuertoRepositorioCliente repositorioCliente;
    private final PuertoCorreo servicioCorreo;
    private final PasswordEncoder codificadorContrasena;
    private final UtilJwt utilJwt;

    @Value("${app.verificacion.minutos-expiracion:15}")
    private int minutosExpiracion;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expiracionJwtMs;

    // 
    // CASO DE USO 1: Registrar usuario - devuelve 200
    // 
    @Override
    @Transactional
    public RespuestaRegistro registrar(PeticionRegistro peticion) {
        log.info("Registrando correo: {}", peticion.getCorreo());

        if (repositorioUsuario.existePorCorreo(peticion.getCorreo())) {
            throw new ExcepcionAutenticacion("El correo ya esta registrado", HttpStatus.CONFLICT);
        }

        // Crear el perfil de cliente vacio (se llena despues con el endpoint de descripcion)
        Cliente clienteNuevo = Cliente.builder()
                .email(peticion.getCorreo())
                .nombre("")
                .descripcion("")
                .build();
        clienteNuevo = repositorioCliente.guardar(clienteNuevo);

        // Crear el usuario ligado al cliente
        Usuario nuevoUsuario = Usuario.builder()
                .correo(peticion.getCorreo())
                .contrasena(codificadorContrasena.encode(peticion.getContrasena()))
                .idCliente(clienteNuevo.getIdCliente())
                .estado(Usuario.EstadoCuenta.PENDIENTE_VERIFICACION)
                .correoVerificado(false)
                .build();

        nuevoUsuario = repositorioUsuario.guardar(nuevoUsuario);
        log.info("Usuario ID: {}, Cliente ID: {}", nuevoUsuario.getId(), clienteNuevo.getIdCliente());

        generarYEnviarCodigo(nuevoUsuario);

        return RespuestaRegistro.builder()
                .id(nuevoUsuario.getId())
                .correo(nuevoUsuario.getCorreo())
                .correoVerificado(false)
                .mensaje("Registro exitoso. Revisa tu correo " + nuevoUsuario.getCorreo() + " para verificar tu cuenta.")
                .build();
    }

    // 
    // CASO DE USO 2: Verificar correo con el codigo
    // Correcto: 201 | Incorrecto: 401
    // 
    @Override
    @Transactional
    public void verificarCorreo(PeticionVerificacion peticion) {
        log.info("Verificando codigo para: {}", peticion.getCorreo());

        Usuario usuario = buscarUsuarioPorCorreo(peticion.getCorreo());

        if (usuario.isCorreoVerificado()) {
            throw new ExcepcionAutenticacion("El correo ya fue verificado anteriormente", HttpStatus.BAD_REQUEST);
        }

        CodigoVerificacion codigo = repositorioCodigo
                .buscarCodigoActivo(
                        usuario.getId(),
                        peticion.getCodigo(),
                        CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO)
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "El codigo es incorrecto o ya expiro", HttpStatus.UNAUTHORIZED));

        if (!codigo.estaVigente()) {
            throw new ExcepcionAutenticacion("El codigo de verificacion ha expirado", HttpStatus.UNAUTHORIZED);
        }

        codigo.setUsado(true);
        repositorioCodigo.guardar(codigo);

        usuario.setCorreoVerificado(true);
        usuario.setEstado(Usuario.EstadoCuenta.ACTIVO);
        repositorioUsuario.guardar(usuario);

        servicioCorreo.enviarBienvenida(usuario.getCorreo(), "");
        log.info("Cuenta activada para usuario ID: {}", usuario.getId());
    }

    // 
    // CASO DE USO 3: Login con correo y contrasena
    // Correcto: 200 con datos del cliente | Incorrecto: 400
    // 
    @Override
    @Transactional(readOnly = true)
    public RespuestaLogin iniciarSesion(PeticionLogin peticion) {
        log.info("Login para: {}", peticion.getCorreo());

        // Si el correo o contrasena son incorrectos devolvemos 400 segun los requerimientos
        Usuario usuario = repositorioUsuario.buscarPorCorreo(peticion.getCorreo())
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "Correo o contrasena incorrectos", HttpStatus.BAD_REQUEST));

        if (!codificadorContrasena.matches(peticion.getContrasena(), usuario.getContrasena())) {
            throw new ExcepcionAutenticacion("Correo o contrasena incorrectos", HttpStatus.BAD_REQUEST);
        }

        if (!usuario.isCorreoVerificado()) {
            throw new ExcepcionAutenticacion(
                    "Debes verificar tu correo antes de iniciar sesion", HttpStatus.UNAUTHORIZED);
        }

        if (usuario.getEstado() != Usuario.EstadoCuenta.ACTIVO) {
            throw new ExcepcionAutenticacion(
                    "Tu cuenta no esta activa. Contacta al soporte.", HttpStatus.UNAUTHORIZED);
        }

        // Buscar los datos del cliente asociado para incluirlos en la respuesta
        Cliente cliente = repositorioCliente.buscarPorId(usuario.getIdCliente()).orElse(null);

        String token = utilJwt.generarToken(usuario.getCorreo(), usuario.getId());

        RespuestaLogin.DatosCliente datosCliente = null;
        if (cliente != null) {
            datosCliente = RespuestaLogin.DatosCliente.builder()
                    .idCliente(cliente.getIdCliente())
                    .nombre(cliente.getNombre())
                    .email(cliente.getEmail())
                    .descripcion(cliente.getDescripcion())
                    .build();
        }

        return RespuestaLogin.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiraEn(expiracionJwtMs / 1000)
                .usuario(RespuestaLogin.DatosUsuario.builder()
                        .id(usuario.getId())
                        .correo(usuario.getCorreo())
                        .estado(usuario.getEstado().name())
                        .cliente(datosCliente)
                        .build())
                .build();
    }

    // 
    // CASO DE USO 4: Guardar descripcion del cliente
    // Credenciales incorrectas: 401 | Correctas: 200 con datos del cliente
    // 
    @Override
    @Transactional
    public RespuestaCliente guardarDescripcion(PeticionDescripcion peticion) {
        log.info("Guardando descripcion para: {}", peticion.getCorreo());

        // Validar credenciales - incorrecto devuelve 401
        Usuario usuario = repositorioUsuario.buscarPorCorreo(peticion.getCorreo())
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "Usuario o contrasena incorrectos", HttpStatus.UNAUTHORIZED));

        if (!codificadorContrasena.matches(peticion.getContrasena(), usuario.getContrasena())) {
            throw new ExcepcionAutenticacion("Usuario o contrasena incorrectos", HttpStatus.UNAUTHORIZED);
        }

        // Buscar el cliente y guardar la descripcion
        Cliente cliente = repositorioCliente.buscarPorId(usuario.getIdCliente())
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "No se encontro el perfil del cliente", HttpStatus.NOT_FOUND));

        cliente.setDescripcion(peticion.getDescripcion());
        cliente = repositorioCliente.guardar(cliente);

        log.info("Descripcion actualizada para cliente ID: {}", cliente.getIdCliente());

        // Devolver todos los datos del cliente EXCEPTO la contrasena
        return RespuestaCliente.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .descripcion(cliente.getDescripcion())
                .idUsuario(usuario.getId())
                .correo(usuario.getCorreo())
                .estado(usuario.getEstado().name())
                .build();
    }

    // 
    // CASO DE USO EXTRA: Reenviar codigo
    // 
    @Override
    @Transactional
    public void reenviarCodigo(String correo) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);

        if (usuario.isCorreoVerificado()) {
            throw new ExcepcionAutenticacion("El correo ya fue verificado", HttpStatus.BAD_REQUEST);
        }

        generarYEnviarCodigo(usuario);
        log.info("Codigo reenviado a: {}", correo);
    }

    // 
    // Metodos privados de ayuda
    // 

    private void generarYEnviarCodigo(Usuario usuario) {
        repositorioCodigo.invalidarCodigosAnteriores(
                usuario.getId(), CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO);

        String nuevoCodigo = GeneradorCodigo.generar(6);

        CodigoVerificacion codigoNuevo = CodigoVerificacion.builder()
                .usuarioId(usuario.getId())
                .codigo(nuevoCodigo)
                .tipo(CodigoVerificacion.TipoCodigo.VERIFICACION_CORREO)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(minutosExpiracion))
                .build();

        repositorioCodigo.guardar(codigoNuevo);
        servicioCorreo.enviarCodigoVerificacion(usuario.getCorreo(), nuevoCodigo);
    }

    private Usuario buscarUsuarioPorCorreo(String correo) {
        return repositorioUsuario.buscarPorCorreo(correo)
                .orElseThrow(() -> new ExcepcionAutenticacion(
                        "Usuario o contrasena incorrectos", HttpStatus.UNAUTHORIZED));
    }
}
