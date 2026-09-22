package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.StockNegativoException;

public class Inventario {
    private int cantidadDisponible;

    public Inventario(int cantidadDisponible) {
        validarCantidad(cantidadDisponible);
        this.cantidadDisponible = cantidadDisponible;
    }

    private void validarCantidad(int cantidadDisponible) {
        if (cantidadDisponible < 0) {
            throw new StockNegativoException();
        }
    }
}
