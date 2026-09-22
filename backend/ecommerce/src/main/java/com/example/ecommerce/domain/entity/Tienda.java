package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.TiendaRequeridaException;

public class Tienda {

    private long id;
    private String nombre;

    public Tienda(long id, String nombre) {
        validarNombre(nombre);
        this.id = id;
        this.nombre = nombre;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new TiendaRequeridaException();
        }
    }
}
