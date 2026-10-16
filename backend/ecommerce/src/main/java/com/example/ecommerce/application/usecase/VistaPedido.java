package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Pedido;

import java.util.Map;
import java.util.Objects;

/**
 * F-07: un pedido junto con los nombres de las personas que aparecen en él
 * (la clienta y quienes cambiaron su estado), para mostrar "quién" en la
 * lista (RF-07.1) y en el historial (RF-07.7) sin que la web consulte
 * usuarios por su cuenta.
 *
 * @param nombresUsuarios id de cuenta → nombre; puede no incluir cuentas que ya no existen
 */
public record VistaPedido(Pedido pedido, Map<Long, String> nombresUsuarios) {

    /** Texto que se muestra cuando una cuenta ya no está disponible. */
    public static final String NOMBRE_DESCONOCIDO = "Cuenta no disponible";

    public VistaPedido {
        Objects.requireNonNull(pedido, "pedido");
        nombresUsuarios = Map.copyOf(nombresUsuarios);
    }

    public String nombreDe(long usuarioId) {
        return nombresUsuarios.getOrDefault(usuarioId, NOMBRE_DESCONOCIDO);
    }

    public String nombreClienta() {
        return nombreDe(pedido.getClienteId());
    }
}
