package com.example.ecommerce.infrastructure.config;

import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase;
import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase.NuevaCuentaPersonal;
import com.example.ecommerce.application.usecase.RegistrarClienteUseCase;
import com.example.ecommerce.application.usecase.RegistrarClienteUseCase.SolicitudRegistro;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Carga una cuenta de demostración por rol para probar el inicio de sesión de
 * cada uno (RF-01.3) sin crearlas a mano. Pasa por los mismos casos de uso que
 * la aplicación real, así que las cuentas cumplen todas las reglas de F-01.
 *
 * Corre en cada arranque y omite las cuentas que ya existen. Se desactiva con
 * {@code app.demo.habilitado=false}, que es lo que debe usarse fuera de
 * desarrollo.
 */
@Component
@ConditionalOnProperty(name = "app.demo.habilitado", havingValue = "true")
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    public static final String PASSWORD_DEMO = "LovelyGirl2026*";

    private final RegistrarClienteUseCase registrarClienteUseCase;
    private final CrearCuentaPersonalUseCase crearCuentaPersonalUseCase;

    public DemoDataLoader(RegistrarClienteUseCase registrarClienteUseCase,
                          CrearCuentaPersonalUseCase crearCuentaPersonalUseCase) {
        this.registrarClienteUseCase = registrarClienteUseCase;
        this.crearCuentaPersonalUseCase = crearCuentaPersonalUseCase;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("cliente@lovelygirl.com", () -> registrarClienteUseCase.ejecutar(new SolicitudRegistro(
                "Camila Ríos", "cliente@lovelygirl.com", "3001234567", PASSWORD_DEMO, true)));
        crearPersonalSiNoExiste("Valentina Torres", "asesora@lovelygirl.com", "3012345678", RolUsuario.ASESORA_VENTAS);
        crearPersonalSiNoExiste("Laura Gómez", "inventario@lovelygirl.com", "3023456789", RolUsuario.ENCARGADA_INVENTARIO);
        crearPersonalSiNoExiste("Yeraldin Noguera", "admin@lovelygirl.com", "3034567890", RolUsuario.ADMINISTRADORA);
    }

    private void crearPersonalSiNoExiste(String nombre, String email, String telefono, RolUsuario rol) {
        crearSiNoExiste(email, () -> crearCuentaPersonalUseCase.ejecutar(
                new NuevaCuentaPersonal(nombre, email, telefono, PASSWORD_DEMO, rol)));
    }

    private void crearSiNoExiste(String email, Runnable creacion) {
        try {
            creacion.run();
        } catch (CorreoElectronicoDuplicadoException ex) {
            log.debug("La cuenta demo '{}' ya existe; se omite.", email);
        }
    }
}
