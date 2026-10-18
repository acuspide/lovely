package com.example.ecommerce.infrastructure.correo;

import com.example.ecommerce.application.NotificadorCorreo;
import com.example.ecommerce.domain.entity.TokenRecuperacion;
import com.example.ecommerce.domain.entity.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Adaptador del puerto NotificadorCorreo por SMTP (Spring Mail). En desarrollo
 * apunta a Mailpit (ver docker-compose.yml), que atrapa los correos y los
 * muestra en http://localhost:8025 sin enviarlos de verdad.
 *
 * Si el envío falla solo se registra el error: propagarlo haría que la
 * respuesta de "olvidé mi contraseña" fuera distinta según el correo exista o
 * no. El enlace nunca se escribe en el log, porque quien lo tenga puede
 * cambiar la contraseña.
 */
public class NotificadorCorreoSmtp implements NotificadorCorreo {

    private static final Logger log = LoggerFactory.getLogger(NotificadorCorreoSmtp.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String urlRestablecimiento;

    public NotificadorCorreoSmtp(JavaMailSender mailSender, String remitente, String urlRestablecimiento) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.urlRestablecimiento = urlRestablecimiento;
    }

    @Override
    public void enviarEnlaceRecuperacion(Usuario destinataria, String tokenRecuperacion) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinataria.getEmail().getValor());
        mensaje.setSubject("Lovely Girl: restablece tu contraseña");
        mensaje.setText(cuerpo(destinataria.getNombre(), enlace(tokenRecuperacion)));

        try {
            mailSender.send(mensaje);
        } catch (MailException ex) {
            log.error("No se pudo enviar el correo de recuperación a la cuenta {}: {}",
                    destinataria.getId(), ex.getMessage());
        }
    }

    private String enlace(String token) {
        return UriComponentsBuilder.fromUriString(urlRestablecimiento)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private static String cuerpo(String nombre, String enlace) {
        long minutos = TokenRecuperacion.VIGENCIA.toMinutes();
        return """
                Hola, %s:

                Recibimos una solicitud para restablecer la contraseña de tu cuenta en Lovely Girl.
                Para crear una contraseña nueva, abre este enlace:

                %s

                El enlace funciona una sola vez y vence en %d minutos.
                Si no fuiste tú, ignora este correo: tu contraseña actual sigue siendo válida.

                Lovely Girl
                """.formatted(nombre, enlace, minutos);
    }
}
