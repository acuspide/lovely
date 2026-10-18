package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.repository.SesionRepository;

import java.time.Clock;

/**
 * RF-01.7: cierre de sesión manual. La sesión queda cerrada en el servidor, así
 * que su token deja de servir aunque siga guardado en el navegador o alguien
 * lo haya copiado.
 */
public class CerrarSesionUseCase {

    private final SesionRepository sesionRepository;
    private final Clock reloj;

    public CerrarSesionUseCase(SesionRepository sesionRepository, Clock reloj) {
        this.sesionRepository = sesionRepository;
        this.reloj = reloj;
    }

    public void ejecutar(String idSesion) {
        sesionRepository.buscarPorId(idSesion).ifPresent(sesion -> {
            sesion.cerrar(reloj.instant());
            sesionRepository.guardar(sesion);
        });
    }
}
