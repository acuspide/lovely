package com.example.ecommerce.domain.exception;

/**
 * RF-01.4: la cuenta queda bloqueada temporalmente tras varios intentos
 * fallidos seguidos. Informa los minutos restantes para que la usuaria sepa
 * cuándo volver a intentarlo.
 */
public class CuentaBloqueadaException extends ReglaDominioException {

    private final long minutosRestantes;

    public CuentaBloqueadaException(long minutosRestantes) {
        super("Tu cuenta está bloqueada temporalmente por varios intentos fallidos. "
                + "Intenta de nuevo en " + minutosRestantes + (minutosRestantes == 1 ? " minuto." : " minutos."));
        this.minutosRestantes = minutosRestantes;
    }

    public long getMinutosRestantes() {
        return minutosRestantes;
    }
}
