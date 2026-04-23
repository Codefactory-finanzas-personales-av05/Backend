package com.finanzas.auth.infraestructura.correo;

import com.finanzas.auth.dominio.puertos.salida.PuertoCorreo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/*
 * Adaptador de correo electronico via n8n webhook.
 *
 * I8 CORREGIDO: RestTemplate inyectado como Bean, no instanciado con new.
 * M5 CORREGIDO: El error de n8n ya no es silencioso — se loguea claramente.
 * M6 CORREGIDO: El codigo ya NO se imprime en texto plano en los logs.
 * M11 CORREGIDO: El tiempo de expiracion viene de la configuracion, no hardcodeado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdaptadorCorreo implements PuertoCorreo {

    // I8: inyectado como Bean desde ConfiguracionApp, no instanciado con new
    private final RestTemplate restTemplate;

    @Value("${app.n8n.webhook-verificacion}")
    private String webhookVerificacion;

    @Value("${app.n8n.webhook-bienvenida}")
    private String webhookBienvenida;

    // M11: tiempo leido desde configuracion, no hardcodeado como "15 minutos"
    @Value("${app.verificacion.minutos-expiracion:15}")
    private int minutosExpiracion;

    @Override
    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        // M6: ya no imprimimos el codigo en texto plano en logs
        log.info("Enviando codigo de verificacion a {} via n8n", destinatario);

        try {
            Map<String, String> cuerpo = new HashMap<>();
            cuerpo.put("correo", destinatario);
            cuerpo.put("codigo", codigo);
            cuerpo.put("tipo", "verificacion");
            cuerpo.put("asunto", "Tu codigo de verificacion - Finance App");
            // M11: usa minutosExpiracion desde la configuracion
            cuerpo.put("mensaje", "Tu codigo de verificacion es: " + codigo
                    + ". Este codigo expira en " + minutosExpiracion + " minutos.");

            llamarWebhook(webhookVerificacion, cuerpo, "verificacion");
            log.info("Codigo enviado exitosamente a {}", destinatario);

        } catch (Exception ex) {
            // M5: error visible, no silencioso. El codigo se puede buscar en la BD.
            log.error("FALLO al enviar codigo a {} via n8n: {}. " +
                    "Buscar el codigo directamente en la tabla codigos_verificacion.", destinatario, ex.getMessage());
        }
    }

    @Override
    public void enviarBienvenida(String destinatario, String nombre) {
        log.info("Enviando bienvenida a {} via n8n", destinatario);

        try {
            Map<String, String> cuerpo = new HashMap<>();
            cuerpo.put("correo", destinatario);
            cuerpo.put("nombre", nombre != null ? nombre : "");
            cuerpo.put("tipo", "bienvenida");
            cuerpo.put("asunto", "Bienvenido a Finance App");
            cuerpo.put("mensaje", "Tu cuenta ha sido verificada. Ya puedes iniciar sesion.");

            llamarWebhook(webhookBienvenida, cuerpo, "bienvenida");
            log.info("Bienvenida enviada exitosamente a {}", destinatario);

        } catch (Exception ex) {
            log.error("FALLO al enviar bienvenida a {} via n8n: {}", destinatario, ex.getMessage());
        }
    }

    private void llamarWebhook(String url, Map<String, String> cuerpo, String tipo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> peticion = new HttpEntity<>(cuerpo, headers);
        ResponseEntity<String> respuesta = restTemplate.postForEntity(url, peticion, String.class);
        log.debug("Webhook n8n ({}) respondio: {}", tipo, respuesta.getStatusCode());
    }
}
