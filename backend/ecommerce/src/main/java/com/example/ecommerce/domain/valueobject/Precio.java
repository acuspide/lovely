package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.PrecioInvalidoException;

import java.math.BigDecimal;
import java.util.Objects;

public class Precio {
    private final BigDecimal valor;

    public Precio(BigDecimal valor) {
        validarPrecio(valor);
        this.valor = valor;
    }

    private void validarPrecio(BigDecimal valor) {
        // RN18: el precio debe ser mayor a cero (no solo no-negativo).
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecioInvalidoException();
        }
    }

    public BigDecimal valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Precio otro)) return false;
        return valor.equals(otro.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
