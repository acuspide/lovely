package com.example.ecommerce.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * RF-08.3 (decisión 2 de F-08): los precios del catálogo ya incluyen el IVA,
 * así que la factura lo separa del total: base + IVA = total. El total de la
 * factura es exactamente lo que pagó la clienta.
 *
 * <p>La base se redondea a centavos y el IVA es la diferencia, de modo que la
 * suma siempre cuadra con el total aunque haya redondeo.</p>
 *
 * @param tasa por ejemplo 0.19 para el 19 %
 */
public record DesgloseIva(BigDecimal base, BigDecimal iva, BigDecimal total, BigDecimal tasa) {

    public DesgloseIva {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(iva, "iva");
        Objects.requireNonNull(total, "total");
        Objects.requireNonNull(tasa, "tasa");
    }

    /** Calcula el desglose de un total que ya incluye el IVA. */
    public static DesgloseIva deTotalConIva(BigDecimal total, BigDecimal tasa) {
        Objects.requireNonNull(total, "total");
        validarTasa(tasa);
        BigDecimal base = total.divide(BigDecimal.ONE.add(tasa), 2, RoundingMode.HALF_UP);
        return new DesgloseIva(base, total.subtract(base), total, tasa);
    }

    /** La tasa debe estar entre 0 (exento) y 1 (100 %). */
    public static void validarTasa(BigDecimal tasa) {
        if (tasa == null || tasa.signum() < 0 || tasa.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("La tasa de IVA debe estar entre 0 y 1.");
        }
    }

    /** Tasa como porcentaje para mostrar ("19"). */
    public String porcentaje() {
        return tasa.movePointRight(2).stripTrailingZeros().toPlainString();
    }
}
