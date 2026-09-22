package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Patrón: Strategy — implementación concreta e intercambiable del puerto
 * PasswordHasher. RNF-05 exige bcrypt explícitamente; si en el futuro se
 * quisiera Argon2 u otro algoritmo, solo se cambia esta clase (OCP): los
 * casos de uso que dependen de PasswordHasher no se tocan.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String contrasenaPlano) {
        return encoder.encode(contrasenaPlano);
    }

    @Override
    public boolean verificar(String contrasenaPlano, String hashAlmacenado) {
        return encoder.matches(contrasenaPlano, hashAlmacenado);
    }
}
