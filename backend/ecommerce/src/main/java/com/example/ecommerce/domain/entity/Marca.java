package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.MarcaRequeridaException;

public class Marca {
    private long id;
    private String nombre;

    public Marca(long id, String nombre) {
        validarNombre(nombre);
        this.id = id;
        this.nombre = nombre;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MarcaRequeridaException();
        }
    }
}
