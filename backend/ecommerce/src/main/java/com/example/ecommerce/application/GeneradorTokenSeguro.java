package com.example.ecommerce.application;

/**
 * Puerto para generar los tokens aleatorios de recuperación de contraseña
 * (RF-01.8) y calcular su huella, que es lo único que se guarda.
 */
public interface GeneradorTokenSeguro {

    /** Token aleatorio, impredecible y apto para ir en una URL. */
    String generar();

    /** Huella determinista e irreversible del token, para buscarlo sin guardarlo. */
    String calcularHuella(String token);
}
