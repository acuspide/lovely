package com.example.ecommerce.domain.exception;

/**
 * RF-02.9 / RN17 / CU-01 A3: un producto no puede eliminarse si tiene pedidos
 * activos. El mensaje sugiere desactivarlo, que lo oculta del catálogo sin
 * romper el historial de esos pedidos.
 */
public class ArticuloConPedidosActivosException extends ReglaDominioException {
    public ArticuloConPedidosActivosException() {
        super("No se puede eliminar este producto porque tiene pedidos activos. "
                + "Puedes desactivarlo para que deje de mostrarse en el catálogo.");
    }
}
