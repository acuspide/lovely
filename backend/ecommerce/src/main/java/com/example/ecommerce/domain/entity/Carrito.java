package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ArticuloNoEncontradoEnCarritoException;
import com.example.ecommerce.domain.valueobject.ItemCarrito;
import com.example.ecommerce.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Carrito {
    private final long id;
    private final long clienteId;
    private final List<ItemCarrito> items;

    public Carrito(long id, long clienteId) {
        this.id = id;
        this.clienteId = clienteId;
        this.items = new ArrayList<>();
    }

    public void agregarItem(long articuloId, int cantidad, Precio precioUnitario, int stockDisponible) {
        Optional<ItemCarrito> itemExistente = buscarItem(articuloId);

        if (itemExistente.isPresent()) {
            ItemCarrito actualizado = itemExistente.get().conNuevaCantidad(cantidad, stockDisponible);
            items.remove(itemExistente.get());
            items.add(actualizado);
        } else {
            items.add(new ItemCarrito(articuloId, cantidad, precioUnitario, stockDisponible));
        }
    }

    public void eliminarItem(long articuloId) {
        ItemCarrito item = buscarItem(articuloId)
                .orElseThrow(ArticuloNoEncontradoEnCarritoException::new);
        items.remove(item);
    }

    public void vaciar() {
        items.clear();
    }

    public BigDecimal calcularTotal() {
        return items.stream()
                .map(ItemCarrito::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    private Optional<ItemCarrito> buscarItem(long articuloId) {
        return items.stream()
                .filter(item -> item.getArticuloId() == articuloId)
                .findFirst();
    }

    public long getId() {
        return id;
    }

    public long getClienteId() {
        return clienteId;
    }

    public List<ItemCarrito> getItems() {
        return List.copyOf(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carrito otro)) return false;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
