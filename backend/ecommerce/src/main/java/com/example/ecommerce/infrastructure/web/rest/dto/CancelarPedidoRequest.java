package com.example.ecommerce.infrastructure.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RF-07.5: la cancelación siempre lleva motivo (el dominio vuelve a validarlo). */
public record CancelarPedidoRequest(
        @NotBlank(message = "Escribe el motivo de la cancelación.")
        @Size(max = 300, message = "El motivo puede tener máximo 300 caracteres.")
        String motivo) {
}
