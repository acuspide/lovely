package com.example.ecommerce.domain.exception;

public class CuentaNoEsPersonalException extends ReglaDominioException {
    public CuentaNoEsPersonalException() {
        super("Solo se pueden gestionar desde este panel las cuentas del personal; las clientas administran su propia cuenta.");
    }
}
