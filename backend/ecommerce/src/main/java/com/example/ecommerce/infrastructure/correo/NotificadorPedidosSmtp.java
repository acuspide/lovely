package com.example.ecommerce.infrastructure.correo;

import com.example.ecommerce.application.NotificadorPedidos;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Patrón: Adapter — envía por SMTP el correo de confirmación del pedido
 * (RF-05.6, CU-02 paso 13). En desarrollo llega a Mailpit
 * (http://localhost:8025).
 *
 * <p>Si el envío falla solo se registra el error: el pedido ya está guardado
 * y la clienta igual ve la confirmación en pantalla.</p>
 */
public class NotificadorPedidosSmtp implements NotificadorPedidos {

    private static final Logger log = LoggerFactory.getLogger(NotificadorPedidosSmtp.class);
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String urlPedidos;

    /**
     * @param urlPedidos dirección base del frontend para ver un pedido (se le agrega el id)
     */
    public NotificadorPedidosSmtp(JavaMailSender mailSender, String remitente, String urlPedidos) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.urlPedidos = urlPedidos.endsWith("/") ? urlPedidos : urlPedidos + "/";
    }

    @Override
    public void enviarConfirmacion(Usuario clienta, Pedido pedido) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(clienta.getEmail().getValor());
        mensaje.setSubject("Lovely Girl: recibimos tu pedido " + pedido.getNumero().valor());
        mensaje.setText(cuerpo(clienta.getNombre(), pedido));

        try {
            mailSender.send(mensaje);
        } catch (MailException ex) {
            log.error("No se pudo enviar la confirmación del pedido {} a la cuenta {}: {}",
                    pedido.getNumero().valor(), clienta.getId(), ex.getMessage());
        }
    }

    private String cuerpo(String nombre, Pedido pedido) {
        String productos = pedido.getDetalles().stream()
                .map(this::linea)
                .collect(Collectors.joining("\n"));
        return """
                Hola, %s:

                ¡Gracias por tu compra! Recibimos tu pedido %s y está en estado Pendiente.

                %s

                Total: %s

                Entrega: %s

                Puedes ver tu pedido aquí: %s%d

                Te avisaremos cuando confirmemos el pago y lo preparemos.

                Lovely Girl
                """.formatted(nombre, pedido.getNumero().valor(), productos, pesos(pedido.calcularTotal()),
                entrega(pedido.getEntrega()), urlPedidos, pedido.getId());
    }

    private String linea(DetallePedido detalle) {
        return "  - " + detalle.getCantidad() + " x " + detalle.getNombreArticulo() + ": " + pesos(detalle.subtotal());
    }

    private static String entrega(DatosEntrega entrega) {
        return switch (entrega.getTipo()) {
            case RETIRO_EN_TIENDA -> "retiro en la tienda de Lovely Girl en Armenia";
            case DOMICILIO -> "domicilio a " + entrega.getDireccion() + ", " + entrega.getCiudad();
            case ENVIO -> "envío nacional a " + entrega.getDireccion() + ", " + entrega.getCiudad();
        };
    }

    private static String pesos(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(COLOMBIA);
        formato.setMaximumFractionDigits(0);
        return "$ " + formato.format(valor);
    }
}
