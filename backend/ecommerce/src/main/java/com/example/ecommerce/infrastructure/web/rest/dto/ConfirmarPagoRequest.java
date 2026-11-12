package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.valueobject.MedioPago;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * F-09 (RF-09.2): datos con los que el personal confirma que el dinero de un
 * pedido llegó. Aquí solo se valida la forma; las reglas (referencia
 * obligatoria en transferencias, monto igual al total, fecha válida) las
 * aplica el dominio.
 */
public record ConfirmarPagoRequest(
        @NotNull(message = "Indica el medio de pago.") MedioPago medio,
        @Size(max = 60, message = "La referencia puede tener máximo 60 caracteres.") String referencia,
        @NotNull(message = "Indica el monto recibido.")
        @Positive(message = "El monto recibido debe ser mayor que cero.")
        @Digits(integer = 10, fraction = 2, message = "El monto recibido no es válido.") BigDecimal monto,
        @NotNull(message = "Indica la fecha en que se recibió el pago.") LocalDate fechaRecepcion) {
}
