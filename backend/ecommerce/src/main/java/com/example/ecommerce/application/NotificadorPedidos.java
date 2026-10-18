package com.example.ecommerce.application;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;

/**
 * Puerto de salida para avisarle a la clienta sobre su pedido (RF-05.6, CU-02
 * paso 13). Separado de {@link NotificadorCorreo} (ISP): quien registra
 * pedidos no necesita saber de recuperación de contraseñas.
 *
 * <p>Si el envío falla, el adaptador lo registra y no lanza la excepción: el
 * pedido ya quedó guardado y no debe deshacerse por un correo.</p>
 */
public interface NotificadorPedidos {

    void enviarConfirmacion(Usuario clienta, Pedido pedido);
}
