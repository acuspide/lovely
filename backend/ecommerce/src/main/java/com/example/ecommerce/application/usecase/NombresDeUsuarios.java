package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.CambioEstadoPedido;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * F-07: arma las {@link VistaPedido} buscando en una sola consulta los
 * nombres de todas las cuentas que aparecen en los pedidos (clientas y
 * personas del historial). Lo comparten los casos de uso de la gestión.
 */
final class NombresDeUsuarios {

    private final UsuarioRepository usuarioRepository;

    NombresDeUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    Map<Long, String> de(Collection<Pedido> pedidos) {
        Set<Long> ids = new HashSet<>();
        for (Pedido pedido : pedidos) {
            ids.add(pedido.getClienteId());
            pedido.getHistorial().stream().map(CambioEstadoPedido::usuarioId).forEach(ids::add);
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        return usuarioRepository.obtenerPorIds(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre, (a, b) -> a));
    }

    VistaPedido vistaDe(Pedido pedido) {
        return new VistaPedido(pedido, de(Set.of(pedido)));
    }
}
