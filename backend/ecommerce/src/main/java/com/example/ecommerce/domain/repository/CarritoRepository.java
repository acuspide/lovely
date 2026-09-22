package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Carrito;

import java.util.Optional;

public interface CarritoRepository {
    Optional<Carrito> obtenerPorId(long id);

    void guardar(Carrito carrito);
}
