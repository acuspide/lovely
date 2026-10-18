package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.TelefonoInvalidoException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Teléfono colombiano de 10 dígitos (celular 3XX o fijo 60X). Acepta el valor
 * con espacios, guiones, paréntesis o el indicativo +57, pero siempre lo guarda
 * normalizado a solo los 10 dígitos, para que dos formas de escribir el mismo
 * número sean iguales.
 */
public class Telefono {

    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[36]\\d{9}$");
    private static final Pattern SEPARADORES = Pattern.compile("[\\s\\-()]");
    private static final String INDICATIVO_COLOMBIA = "57";

    private final String valor;

    public Telefono(String valor) {
        this.valor = normalizar(valor);
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            throw new TelefonoInvalidoException();
        }

        String digitos = SEPARADORES.matcher(valor).replaceAll("");
        if (digitos.startsWith("+")) {
            digitos = digitos.substring(1);
        }
        if (digitos.length() == 12 && digitos.startsWith(INDICATIVO_COLOMBIA)) {
            digitos = digitos.substring(INDICATIVO_COLOMBIA.length());
        }

        if (!PATRON_TELEFONO.matcher(digitos).matches()) {
            throw new TelefonoInvalidoException();
        }
        return digitos;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Telefono otro)) return false;
        return valor.equals(otro.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
