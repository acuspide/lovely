package com.example.ecommerce.infrastructure.web.rest.dto;

/** RF-01.6: tiempo que le queda a la sesión tras pulsar "Seguir conectado". */
public record SesionRenovadaResponse(long segundosParaExpirar) {
}
