package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.GeneradorTokenSeguro;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Tokens de recuperación de 256 bits generados con SecureRandom, codificados
 * en Base64 URL-safe para ir en el enlace del correo. La huella es SHA-256: al
 * ser el token aleatorio y largo, no hace falta un hash lento como bcrypt.
 */
public class GeneradorTokenSeguroSha256 implements GeneradorTokenSeguro {

    private static final int BYTES_DE_ENTROPIA = 32;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String generar() {
        byte[] bytes = new byte[BYTES_DE_ENTROPIA];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String calcularHuella(String token) {
        try {
            byte[] huella = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(huella);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("La JVM no soporta SHA-256.", ex);
        }
    }
}
