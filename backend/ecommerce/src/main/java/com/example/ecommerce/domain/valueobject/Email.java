package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.CorreoElectronicoInvalidoException;
import com.example.ecommerce.domain.exception.ReglaDominioException;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {

    private static final Pattern PATRON_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final String valor;

    public Email(String valor) {
        validarEmail(valor);
        this.valor = valor.trim().toLowerCase();
    }

    private void validarEmail(String valor) {
        if (valor == null || valor.trim().isEmpty()
                || !PATRON_EMAIL.matcher(valor.trim()).matches()) {
            throw new CorreoElectronicoInvalidoException();
        }
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email otro)) return false;
        return valor.equals(otro.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}