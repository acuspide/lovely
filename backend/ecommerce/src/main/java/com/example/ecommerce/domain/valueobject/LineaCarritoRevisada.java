package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.CantidadInvalidaException;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Patrón: Value Object — una línea del carrito ya comparada con el inventario
 * (CO-01 verificarStock, RF-04.1, RF-04.2 y RF-04.7).
 *
 * <p>El carrito vive en el navegador de la clienta, pero el navegador solo
 * aporta <b>qué</b> producto quiere y <b>cuántas</b> unidades. El precio, el
 * stock y si el producto se puede vender salen siempre del producto guardado:
 * así nadie puede comprar a otro precio ni más unidades de las que hay.</p>
 *
 * <p>Esta clase no modifica el inventario (es de solo lectura, como CO-01).</p>
 */
public final class LineaCarritoRevisada {

    private final long articuloId;
    private final Articulo articulo;
    private final int cantidadSolicitada;
    private final int cantidad;
    private final EstadoLineaCarrito estado;

    private LineaCarritoRevisada(long articuloId, Articulo articulo, int cantidadSolicitada,
                                 int cantidad, EstadoLineaCarrito estado) {
        this.articuloId = articuloId;
        this.articulo = articulo;
        this.cantidadSolicitada = cantidadSolicitada;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    /**
     * Revisa una línea del carrito.
     *
     * @param articuloId         producto que pide la clienta
     * @param articulo           el producto guardado, o vacío si ya no existe
     * @param cantidadSolicitada unidades pedidas (RN19: entero mayor a cero)
     */
    public static LineaCarritoRevisada revisar(long articuloId, Optional<Articulo> articulo, int cantidadSolicitada) {
        if (cantidadSolicitada <= 0) {
            throw new CantidadInvalidaException();
        }
        if (articulo.isEmpty() || !articulo.get().isActivo()) {
            return new LineaCarritoRevisada(articuloId, articulo.orElse(null), cantidadSolicitada, 0,
                    EstadoLineaCarrito.NO_DISPONIBLE);
        }

        Articulo producto = articulo.get();
        if (producto.estaAgotado()) {
            return new LineaCarritoRevisada(articuloId, producto, cantidadSolicitada, 0, EstadoLineaCarrito.AGOTADO);
        }
        if (cantidadSolicitada > producto.getStock()) {
            // CU-02 A2: se ajusta a la cantidad máxima permitida.
            return new LineaCarritoRevisada(articuloId, producto, cantidadSolicitada, producto.getStock(),
                    EstadoLineaCarrito.AJUSTADA);
        }
        return new LineaCarritoRevisada(articuloId, producto, cantidadSolicitada, cantidadSolicitada,
                EstadoLineaCarrito.DISPONIBLE);
    }

    /** Precio actual × cantidad que sí se puede comprar (0 si la línea no se puede comprar). */
    public BigDecimal subtotal() {
        if (!estado.sePuedeComprar()) {
            return BigDecimal.ZERO;
        }
        return articulo.getPrecio().valor().multiply(BigDecimal.valueOf(cantidad));
    }

    /** true si la clienta debe enterarse de algo (cantidad ajustada, agotado o retirado). */
    public boolean requiereAtencion() {
        return estado != EstadoLineaCarrito.DISPONIBLE;
    }

    public long getArticuloId() { return articuloId; }
    /** El producto guardado; vacío si ya no existe. */
    public Optional<Articulo> getArticulo() { return Optional.ofNullable(articulo); }
    public int getCantidadSolicitada() { return cantidadSolicitada; }
    /** Unidades que sí se pueden comprar (0 si está agotado o no disponible). */
    public int getCantidad() { return cantidad; }
    public EstadoLineaCarrito getEstado() { return estado; }
}
