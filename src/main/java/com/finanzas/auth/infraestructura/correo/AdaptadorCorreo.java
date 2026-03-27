package com.finanzas.auth.infraestructura.correo;

import com.finanzas.auth.dominio.puertos.salida.PuertoCorreo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/*
 * Adaptador de SALIDA para el correo electronico.
 */
@Component
@Slf4j
public class AdaptadorCorreo implements PuertoCorreo {

    @Override
    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        // TODO: cuando tengamos SMTP real, reemplazar esto por JavaMailSender
        log.info("================================================");
        log.info("  CODIGO DE VERIFICACION");
        log.info("  Para: {}", destinatario);
        log.info("  Codigo: {}", codigo);
        log.info("  (En produccion esto llega al correo real)");
        log.info("================================================");
    }

    @Override
    public void enviarBienvenida(String destinatario, String nombre) {
        log.info("================================================");
        log.info("  CORREO DE BIENVENIDA enviado a: {}", destinatario);
        log.info("================================================");
    }
}
