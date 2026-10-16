package com.example.ecommerce.domain.service;

import com.example.ecommerce.domain.exception.ContrasenaDebilException;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * RF-01.1 / RF-01.8: reglas que debe cumplir toda contraseña nueva (registro,
 * alta de personal, recuperación y cambio desde el perfil). Se valida sobre el
 * texto plano antes de cifrarlo, que es el único momento en que se conoce.
 *
 * Reglas: entre 8 y 20 caracteres, con al menos una letra, un número y un
 * carácter especial (cualquier símbolo que no sea letra, número ni espacio).
 *
 * Además se conserva el tope de 72 bytes UTF-8 porque bcrypt solo usa los
 * primeros 72 bytes (y Spring Security rechaza las más largas). Con 20
 * caracteres casi nunca se alcanza, pero algunos emojis ocupan 4 bytes.
 */
public final class PoliticaContrasena {

    public static final int LONGITUD_MINIMA = 8;
    public static final int LONGITUD_MAXIMA = 20;
    public static final int LONGITUD_MAXIMA_BYTES = 72;

    private static final Pattern CONTIENE_LETRA = Pattern.compile("\\p{L}");
    private static final Pattern CONTIENE_NUMERO = Pattern.compile("\\p{Nd}");
    private static final Pattern CONTIENE_ESPECIAL = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private PoliticaContrasena() {
    }

    public static void validar(String contrasenaPlano) {
        if (contrasenaPlano == null) {
            throw new ContrasenaDebilException();
        }
        int caracteres = contrasenaPlano.codePointCount(0, contrasenaPlano.length());
        if (caracteres < LONGITUD_MINIMA
                || caracteres > LONGITUD_MAXIMA
                || contrasenaPlano.getBytes(StandardCharsets.UTF_8).length > LONGITUD_MAXIMA_BYTES
                || !CONTIENE_LETRA.matcher(contrasenaPlano).find()
                || !CONTIENE_NUMERO.matcher(contrasenaPlano).find()
                || !CONTIENE_ESPECIAL.matcher(contrasenaPlano).find()) {
            throw new ContrasenaDebilException();
        }
    }
}
