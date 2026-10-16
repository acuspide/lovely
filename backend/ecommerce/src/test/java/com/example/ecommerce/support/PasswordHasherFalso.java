package com.example.ecommerce.support;

import com.example.ecommerce.application.PasswordHasher;

/**
 * Doble rápido del puerto PasswordHasher para las pruebas de casos de uso.
 * El cifrado real con bcrypt se prueba aparte en BCryptPasswordHasherTest.
 */
public class PasswordHasherFalso implements PasswordHasher {

    private static final String PREFIJO = "hash-falso:";

    @Override
    public String hash(String contrasenaPlano) {
        return PREFIJO + contrasenaPlano;
    }

    @Override
    public boolean verificar(String contrasenaPlano, String hashAlmacenado) {
        return hashAlmacenado.equals(PREFIJO + contrasenaPlano);
    }
}
