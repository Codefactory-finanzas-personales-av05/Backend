package com.finanzas.auth.infraestructura.correo;

import com.finanzas.auth.dominio.puertos.salida.PuertoCorreo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/*
 * Adaptador de SALIDA para correo electronico usando n8n como intermediario.
 *
 * En vez de enviar el correo directamente desde Spring Boot,
 * le mandamos los datos a n8n por webhook y n8n se encarga
 * de enviarlo por Gmail o Hotmail segun como este configurado el workflow.
 *
 * Si n8n falla, el proceso de registro NO se interrumpe.
 * Solo se registra el error en los logs y el codigo igual aparece alli.
 */
@Component
@Slf4j
public class AdaptadorCorreo implements PuertoCorreo {

    @Value("${app.n8n.webhook-verificacion}")
    private String webhookVerificacion;

    @Value("${app.n8n.webhook-bienvenida}")
    private String webhookBienvenida;

    // RestTemplate es el cliente HTTP que Spring usa para hacer peticiones externas
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        log.info("Enviando codigo de verificacion a {} via n8n", destinatario);

        // Siempre imprimimos el codigo en logs como respaldo
        log.info("================================================");
        log.info("  CODIGO DE VERIFICACION (respaldo en logs)");
        log.info("  Para: {}", destinatario);
        log.info("  Codigo: {}", codigo);
        log.info("================================================");

        try {
            Map<String, String> cuerpo = new HashMap<>();
            cuerpo.put("correo", destinatario);
            cuerpo.put("codigo", codigo);
            cuerpo.put("tipo", "verificacion");
            cuerpo.put("asunto", "Tu codigo de verificacion - Finance App");
            cuerpo.put("mensaje", "Tu codigo de verificacion es: " + codigo +
                    ". Este codigo expira en 15 minutos.");

            llamarWebhook(webhookVerificacion, cuerpo, "verificacion");

        } catch (Exception ex) {
            // Si n8n falla, el registro continua normalmente
            // El usuario puede ver el codigo en los logs
            log.error("No se pudo enviar el correo via n8n: {}", ex.getMessage());
        }
    }

    @Override
    public void enviarBienvenida(String destinatario, String nombre) {
        log.info("Enviando correo de bienvenida a {} via n8n", destinatario);

        try {
            Map<String, String> cuerpo = new HashMap<>();
            cuerpo.put("correo", destinatario);
            cuerpo.put("nombre", nombre != null ? nombre : "");
            cuerpo.put("tipo", "bienvenida");
            cuerpo.put("asunto", "Bienvenido a Finance App");
            cuerpo.put("mensaje", "Tu cuenta ha sido verificada exitosamente. Ya puedes iniciar sesion.");

            llamarWebhook(webhookBienvenida, cuerpo, "bienvenida");

        } catch (Exception ex) {
            log.error("No se pudo enviar bienvenida via n8n: {}", ex.getMessage());
        }
    }

    // Metodo privado reutilizable para llamar cualquier webhook de n8n
    private void llamarWebhook(String url, Map<String, String> cuerpo, String tipo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> peticion = new HttpEntity<>(cuerpo, headers);

        ResponseEntity<String> respuesta = restTemplate.postForEntity(url, peticion, String.class);

        log.info("Webhook n8n ({}) respondio con status: {}", tipo, respuesta.getStatusCode());
    }
}
