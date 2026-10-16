package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.valueobject.Telefono;

/**
 * Para el personal el teléfono es opcional: un campo vacío significa "sin
 * teléfono", pero uno escrito con un formato inválido sigue siendo un error.
 */
final class TelefonoOpcional {

    private TelefonoOpcional() {
    }

    static Telefono desde(String valor) {
        return valor == null || valor.isBlank() ? null : new Telefono(valor);
    }
}
