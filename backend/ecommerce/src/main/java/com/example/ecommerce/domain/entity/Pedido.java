package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.PedidoSinDetallesException;
import com.example.ecommerce.domain.exception.TransicionEstadoInvalidaException;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {
    private final long id;
    private final long clienteId;
    private final List<DetallePedido> detalles;
    private EstadoPedido estado;

    public Pedido(long id, long clienteId) {
        this.id = id;
        this.clienteId = clienteId;
        this.detalles = new ArrayList<>();
        this.estado = EstadoPedido.PENDIENTE;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
    }

    public void confirmar() {
        if (detalles.isEmpty()) {
            throw new PedidoSinDetallesException();
        }
        transicionarA(EstadoPedido.CONFIRMADO);
    }

    public void cancelar() {
        transicionarA(EstadoPedido.CANCELADO);
    }

    private void transicionarA(EstadoPedido nuevoEstado) {
        if (!estado.puedeTransicionarA(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(estado.name(), nuevoEstado.name());
        }
        this.estado = nuevoEstado;
    }

    public BigDecimal calcularTotal() {
        return detalles.stream()
                .map(DetallePedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getId() {
        return id;
    }

    public long getClienteId() {
        return clienteId;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<DetallePedido> getDetalles() {
        return List.copyOf(detalles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido otro)) return false;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
