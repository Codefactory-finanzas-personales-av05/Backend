package com.finanzas.auth.dominio.puertos.salida;

/*
 * 
 * El dominio no sabe si se usa Gmail, SendGrid o solo se imprime en consola.
 */
public interface PuertoCorreo {

    void enviarCodigoVerificacion(String destinatario, String codigo);

    void enviarBienvenida(String destinatario, String nombre);
}
