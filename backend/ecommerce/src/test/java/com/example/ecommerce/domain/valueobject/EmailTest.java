package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmailTest {

    @Test
    void debeCrearEmailValidoYNormalizarloAMinusculas() {
        // Arrange y Act
        Email email = new Email("Usuario@Ejemplo.com");

        // Assert
        assertEquals("usuario@ejemplo.com", email.getValor());
    }

    @Test
    void dosEmailsConElMismoValorDebenSerIguales() {
        // Arrange
        Email email1 = new Email("test@correo.com");
        Email email2 = new Email("TEST@correo.com");

        // Act y Assert
        assertEquals(email1, email2);
    }

    @Test
    void unCorreoConFormatoInvalidoDebeLanzarReglaDominioException() {
        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            new Email("correo-invalido");
        });
    }
}