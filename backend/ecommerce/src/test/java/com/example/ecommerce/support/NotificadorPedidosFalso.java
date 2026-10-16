package com.example.ecommerce.support;

import com.example.ecommerce.application.NotificadorPedidos;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;

import java.util.ArrayList;
import java.util.List;

/** Doble del puerto NotificadorPedidos: guarda los números de pedido "enviados". */
public class NotificadorPedidosFalso implements NotificadorPedidos {

    private final List<String> enviados = new ArrayList<>();

    @Override
    public void enviarConfirmacion(Usuario clienta, Pedido pedido) {
        enviados.add(clienta.getEmail().getValor() + " " + pedido.getNumero().valor());
    }

    public List<String> enviados() {
        return List.copyOf(enviados);
    }
}
