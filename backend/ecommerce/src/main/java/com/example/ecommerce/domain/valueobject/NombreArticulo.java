package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.NombreArticuloRequeridoException;

import java.util.Objects;

public class NombreArticulo {

    private final String valor;

    public NombreArticulo(String valor) {
        validarNombre(valor);
        this.valor = valor;
    }

    private void validarNombre(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new NombreArticuloRequeridoException();
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NombreArticulo otro)) return false;
        return valor.equals(otro.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
