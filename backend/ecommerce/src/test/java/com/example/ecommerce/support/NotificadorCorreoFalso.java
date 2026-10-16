package com.example.ecommerce.support;

import com.example.ecommerce.application.NotificadorCorreo;
import com.example.ecommerce.domain.entity.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Doble del puerto NotificadorCorreo: guarda los correos "enviados" para revisarlos. */
public class NotificadorCorreoFalso implements NotificadorCorreo {

    public record CorreoEnviado(String destinataria, String token) {
    }

    private final List<CorreoEnviado> enviados = new ArrayList<>();

    @Override
    public void enviarEnlaceRecuperacion(Usuario destinataria, String tokenRecuperacion) {
        enviados.add(new CorreoEnviado(destinataria.getEmail().getValor(), tokenRecuperacion));
    }

    public List<CorreoEnviado> enviados() {
        return List.copyOf(enviados);
    }

    public Optional<String> ultimoToken() {
        return enviados.isEmpty() ? Optional.empty() : Optional.of(enviados.getLast().token());
    }
}
