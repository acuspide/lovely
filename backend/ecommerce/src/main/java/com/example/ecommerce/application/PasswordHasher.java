package com.example.ecommerce.application;

public interface PasswordHasher {
    String hash(String contrasenaPlano);
    boolean verificar(String contrasenaPlano, String hashAlmacenado);
}