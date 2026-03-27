package com.finanzas.auth.compartido.utilidad;

import java.security.SecureRandom;

/*
 * Utilidad para generar codigos de verificacion numericos.
 */
public final class GeneradorCodigo {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final String DIGITOS = "0123456789";

    // No se puede instanciar esta clase
    private GeneradorCodigo() {}

    public static String generar(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(DIGITOS.charAt(ALEATORIO.nextInt(DIGITOS.length())));
        }
        return sb.toString();
    }
}
