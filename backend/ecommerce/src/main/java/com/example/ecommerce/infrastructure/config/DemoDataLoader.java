package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.usecase.RegistrarUsuarioUseCase;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga un usuario de demostración por cada rol de RolUsuario, para poder
 * probar el login por rol (RF-01) sin pasar primero por "Crear cuenta". Con
 * Oracle persistente (a diferencia del prototipo en memoria) esto corre en
 * cada arranque, así que cada registro se ignora si el correo ya existe.
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    public static final String PASSWORD_DEMO = "LovelyGirl2026*";

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    public DemoDataLoader(RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
    }

    @Override
    public void run(String... args) {
        registrarSiNoExiste("Camila Ríos", "cliente@lovelygirl.com", RolUsuario.CLIENTE);
        registrarSiNoExiste("Valentina Torres", "asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);
        registrarSiNoExiste("Laura Gómez", "inventario@lovelygirl.com", RolUsuario.ENCARGADA_INVENTARIO);
        registrarSiNoExiste("Yeraldin Noguera", "admin@lovelygirl.com", RolUsuario.ADMINISTRADORA);
    }

    private void registrarSiNoExiste(String nombre, String email, RolUsuario rol) {
        try {
            registrarUsuarioUseCase.ejecutar(nombre, email, PASSWORD_DEMO, rol);
        } catch (CorreoElectronicoDuplicadoException ex) {
            log.debug("Usuario demo '{}' ya existe, se omite.", email);
        }
    }
}
