package com.example.ecommerce.infrastructure.security;

import com.example.ecommerce.application.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Patrón: Strategy — implementación concreta e intercambiable del puerto
 * PasswordHasher. RF-01.5 / RNF-05 exigen bcrypt con un factor de costo de al
 * menos 10; si en el futuro se quisiera otro algoritmo, solo se cambia esta
 * clase (OCP).
 */
public class BCryptPasswordHasher implements PasswordHasher {

    /** RF-01.5: costo mínimo aceptado. */
    public static final int COSTO_MINIMO = 10;

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasher(int costo) {
        if (costo < COSTO_MINIMO) {
            throw new IllegalArgumentException(
                    "El costo de bcrypt debe ser al menos " + COSTO_MINIMO + " (RF-01.5); se configuró " + costo + ".");
        }
        this.encoder = new BCryptPasswordEncoder(costo);
    }

    @Override
    public String hash(String contrasenaPlano) {
        return encoder.encode(contrasenaPlano);
    }

    @Override
    public boolean verificar(String contrasenaPlano, String hashAlmacenado) {
        return contrasenaPlano != null && encoder.matches(contrasenaPlano, hashAlmacenado);
    }
}
