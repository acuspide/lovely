package com.example.ecommerce.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-01.5: contraseñas cifradas con bcrypt y factor de costo mínimo 10. */
class BCryptPasswordHasherTest {

    @Test
    void elHashEsBcryptConElCostoConfiguradoYNoContieneLaContrasena() {
        // Arrange
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(10);

        // Act
        String hash = hasher.hash("Lovely2026*");

        // Assert
        assertTrue(hash.matches("^\\$2[aby]\\$10\\$.{53}$"), "Debe ser un hash bcrypt de costo 10: " + hash);
        assertFalse(hash.contains("Lovely2026*"));
    }

    @Test
    void laMismaContrasenaProduceHashesDistintosPorLaSal() {
        // Arrange
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(10);

        // Act y Assert
        assertNotEquals(hasher.hash("Lovely2026*"), hasher.hash("Lovely2026*"));
    }

    @Test
    void verificaLaContrasenaCorrectaYRechazaLaIncorrecta() {
        // Arrange
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(10);
        String hash = hasher.hash("Lovely2026*");

        // Act y Assert
        assertTrue(hasher.verificar("Lovely2026*", hash));
        assertFalse(hasher.verificar("lovely2026", hash));
        assertFalse(hasher.verificar(null, hash));
    }

    @Test
    void noPermiteConfigurarUnCostoMenorADiez() {
        // Act y Assert
        assertThrows(IllegalArgumentException.class, () -> new BCryptPasswordHasher(9));
    }
}
