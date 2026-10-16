package com.example.ecommerce.application;

import com.example.ecommerce.domain.entity.Usuario;

/**
 * Puerto de salida para los correos que envía el sistema. La forma del enlace
 * (URL del frontend) y el medio de envío (SMTP) son detalles de
 * infraestructura.
 *
 * RF-01.5: ningún correo lleva la contraseña; la recuperación se hace solo con
 * un enlace de un solo uso.
 */
public interface NotificadorCorreo {

    void enviarEnlaceRecuperacion(Usuario destinataria, String tokenRecuperacion);
}
