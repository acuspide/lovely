package com.example.ecommerce.domain.valueobject;

import java.math.BigDecimal;
import java.util.List;

/**
 * Patrón: Value Object — el carrito completo revisado contra el inventario
 * (RF-04.3 y RF-04.7).
 *
 * <p>Se usa en dos momentos: cada vez que la clienta agrega o cambia algo
 * (verificación en tiempo real) y justo antes de confirmar el pedido (CU-02
 * paso 10). Si {@link #requiereAtencion()} es true, la clienta debe revisar
 * los cambios antes de continuar (CU-02, curso alterno A3).</p>
 */
public final class RevisionCarrito {

    private final List<LineaCarritoRevisada> lineas;

    public RevisionCarrito(List<LineaCarritoRevisada> lineas) {
        this.lineas = List.copyOf(lineas);
    }

    public List<LineaCarritoRevisada> getLineas() {
        return lineas;
    }

    /** Total en pesos de lo que sí se puede comprar, con los precios actuales. */
    public BigDecimal total() {
        return lineas.stream()
                .map(LineaCarritoRevisada::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Unidades que se pueden comprar, sumando todas las líneas. */
    public int cantidadDeUnidades() {
        return lineas.stream().mapToInt(LineaCarritoRevisada::getCantidad).sum();
    }

    /** true si alguna línea cambió (ajustada, agotada o retirada). */
    public boolean requiereAtencion() {
        return lineas.stream().anyMatch(LineaCarritoRevisada::requiereAtencion);
    }

    /** true si queda al menos una línea que se pueda comprar. */
    public boolean tieneProductosComprables() {
        return lineas.stream().anyMatch(linea -> linea.getEstado().sePuedeComprar());
    }
}
