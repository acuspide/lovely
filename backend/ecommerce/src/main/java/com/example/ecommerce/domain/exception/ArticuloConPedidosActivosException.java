package com.example.ecommerce.domain.exception;

/** RN17: un producto no puede eliminarse si tiene pedidos activos. */
public class ArticuloConPedidosActivosException extends ReglaDominioException {
    public ArticuloConPedidosActivosException() {
        super("No se puede eliminar: el producto tiene pedidos activos.");
    }
}
