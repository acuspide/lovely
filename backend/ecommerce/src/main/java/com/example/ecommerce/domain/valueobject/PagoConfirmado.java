package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.PagoInvalidoException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * F-09 / RN11: constancia de que Lovely Girl recibió el dinero de un pedido.
 *
 * <p>No es un comprobante que reporta la clienta: es la verificación que hace
 * el personal (Asesora de ventas o Administradora) después de revisar la
 * cuenta bancaria o de recibir el efectivo.</p>
 *
 * <ul>
 *   <li>RF-09.2: medio, referencia, monto recibido, fecha de recepción, quién
 *       confirmó y cuándo.</li>
 *   <li>RF-09.2: la referencia es obligatoria en transferencias (4 a 60
 *       caracteres) y opcional en efectivo.</li>
 *   <li>El monto debe ser mayor que cero; que coincida con el total del pedido
 *       lo valida el pedido (RF-09.3).</li>
 * </ul>
 */
public record PagoConfirmado(MedioPago medio, String referencia, BigDecimal monto, LocalDate fechaRecepcion,
                             long confirmadoPor, Instant confirmadoEn) {

    public static final int MIN_REFERENCIA = 4;
    public static final int MAX_REFERENCIA = 60;

    public PagoConfirmado {
        if (medio == null) {
            throw new PagoInvalidoException("Indica el medio de pago.");
        }
        referencia = referencia == null || referencia.isBlank() ? null : referencia.strip();
        if (medio.exigeReferencia() && referencia == null) {
            throw new PagoInvalidoException("Escribe la referencia de la transferencia (número de la transacción).");
        }
        if (referencia != null && (referencia.length() < MIN_REFERENCIA || referencia.length() > MAX_REFERENCIA)) {
            throw new PagoInvalidoException(
                    "La referencia debe tener entre " + MIN_REFERENCIA + " y " + MAX_REFERENCIA + " caracteres.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new PagoInvalidoException("El monto recibido debe ser mayor que cero.");
        }
        if (fechaRecepcion == null) {
            throw new PagoInvalidoException("Indica la fecha en que se recibió el pago.");
        }
        Objects.requireNonNull(confirmadoEn, "confirmadoEn");
    }
}
