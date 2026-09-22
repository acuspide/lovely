package com.example.ecommerce.infrastructure.web.rest.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(@NotNull(message = "Debes indicar el nuevo estado.") Boolean activo) {
}
