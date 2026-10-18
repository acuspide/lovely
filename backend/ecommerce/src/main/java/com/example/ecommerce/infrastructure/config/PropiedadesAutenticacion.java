package com.example.ecommerce.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Parámetros de F-01 leídos de application.properties (prefijo
 * {@code app.autenticacion}). Se validan al arrancar: una configuración que
 * incumpla un requisito (por ejemplo, bcrypt con costo menor a 10) impide que
 * la aplicación inicie, en vez de fallar en silencio.
 *
 * @param bcryptCosto              RF-01.5: factor de costo de bcrypt (mínimo 10).
 * @param minutosInactividad       RF-01.6: minutos sin actividad tras los que vence la sesión.
 * @param horasMaximasToken        tope absoluto de vida del token, aunque haya actividad.
 * @param jwtSecreto               clave HMAC para firmar los tokens (mínimo 256 bits).
 * @param urlRestablecerContrasena RF-01.8: página del frontend a la que apunta el enlace del correo.
 * @param remitenteCorreo          dirección desde la que se envían los correos.
 */
@Validated
@ConfigurationProperties(prefix = "app.autenticacion")
public record PropiedadesAutenticacion(
        @Min(10) int bcryptCosto,
        @Min(1) long minutosInactividad,
        @Min(1) long horasMaximasToken,
        @NotBlank @Size(min = 32) String jwtSecreto,
        @NotBlank String urlRestablecerContrasena,
        @NotBlank String remitenteCorreo) {

    public Duration inactividadMaxima() {
        return Duration.ofMinutes(minutosInactividad);
    }

    public Duration duracionMaximaToken() {
        return Duration.ofHours(horasMaximasToken);
    }

    /** El secreto nunca debe aparecer en logs ni en mensajes de error. */
    @Override
    public String toString() {
        return "PropiedadesAutenticacion{bcryptCosto=" + bcryptCosto
                + ", minutosInactividad=" + minutosInactividad
                + ", horasMaximasToken=" + horasMaximasToken + "}";
    }
}
