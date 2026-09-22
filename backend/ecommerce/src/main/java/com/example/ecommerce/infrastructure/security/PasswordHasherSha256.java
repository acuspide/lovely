package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.PasswordHasher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasherSha256 implements PasswordHasher {

    private static final String ALGORITMO = "SHA-256";
    private static final int TAMANO_SALT = 16;

    private final SecureRandom generadorAleatorio = new SecureRandom();

    @Override
    public String hash(String contrasenaPlano) {
        byte[] salt = new byte[TAMANO_SALT];
        generadorAleatorio.nextBytes(salt);

        String digestoBase64 = digerir(contrasenaPlano, salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);

        return saltBase64 + ":" + digestoBase64;
    }

    @Override
    public boolean verificar(String contrasenaPlano, String hashAlmacenado) {
        String[] partes = hashAlmacenado.split(":", 2);
        if (partes.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(partes[0]);
        String digestoEsperado = partes[1];
        String digestoCalculado = digerir(contrasenaPlano, salt);

        return digestoCalculado.equals(digestoEsperado);
    }

    private String digerir(String contrasenaPlano, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            digest.update(salt);
            byte[] resultado = digest.digest(contrasenaPlano.getBytes());
            return Base64.getEncoder().encodeToString(resultado);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hashing no disponible: " + ALGORITMO, e);
        }
    }
}
