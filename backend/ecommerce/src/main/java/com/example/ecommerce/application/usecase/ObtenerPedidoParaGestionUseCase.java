package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;

/**
 * RF-07.2: detalle completo de un pedido para el personal: productos,
 * cantidades, precios, total, datos de contacto y entrega, y el historial de
 * estados con quién hizo cada cambio (RF-07.7).
 */
public class ObtenerPedidoParaGestionUseCase {

    private final PedidoRepository pedidoRepository;
    private final NombresDeUsuarios nombres;

    public ObtenerPedidoParaGestionUseCase(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.nombres = new NombresDeUsuarios(usuarioRepository);
    }

    public VistaPedido ejecutar(long pedidoId) {
        return pedidoRepository.obtenerPorId(pedidoId)
                .map(nombres::vistaDe)
                .orElseThrow(PedidoNoEncontradoException::new);
    }
}
