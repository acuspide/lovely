package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.CantidadInvalidaException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Patrón: Value Object — una línea de un pedido (RF-05.3).
 *
 * <p>Guarda una "foto" del producto en el momento de la compra: su nombre y
 * su precio unitario. Si después la Administradora cambia el precio o el
 * nombre del producto, el pedido ya registrado no cambia.</p>
 */
public class DetallePedido {

    private final long articuloId;
    private final String nombreArticulo;
    private final int cantidad;
    private final Precio precioUnitario;

    public DetallePedido(long articuloId, String nombreArticulo, int cantidad, Precio precioUnitario) {
        validarCantidad(cantidad);
        this.articuloId = articuloId;
        this.nombreArticulo = Objects.requireNonNull(nombreArticulo, "nombreArticulo");
        this.cantidad = cantidad;
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "precioUnitario");
    }

    /** RN19: la cantidad es un entero mayor a cero. */
    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException();
        }
    }

    public BigDecimal subtotal() {
        return precioUnitario.valor().multiply(BigDecimal.valueOf(cantidad));
    }

    public long getArticuloId() { return articuloId; }
    /** Nombre del producto al momento de la compra. */
    public String getNombreArticulo() { return nombreArticulo; }
    public int getCantidad() { return cantidad; }
    /** Precio unitario al momento de la compra. */
    public Precio getPrecioUnitario() { return precioUnitario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedido otro)) return false;
        return articuloId == otro.articuloId
                && cantidad == otro.cantidad
                && nombreArticulo.equals(otro.nombreArticulo)
                && precioUnitario.equals(otro.precioUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articuloId, nombreArticulo, cantidad, precioUnitario);
    }
}
