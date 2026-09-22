package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Carrito;
import com.example.ecommerce.domain.repository.CarritoRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CarritoRepositoryEnMemoria implements CarritoRepository {

    private final Map<Long, Carrito> carritos = new HashMap<>();

    @Override
    public Optional<Carrito> obtenerPorId(long id) {
        return Optional.ofNullable(carritos.get(id));
    }

    @Override
    public void guardar(Carrito carrito) {
        carritos.put(carrito.getId(), carrito);
    }
}
