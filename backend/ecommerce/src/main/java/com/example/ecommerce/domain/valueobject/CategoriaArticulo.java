package com.example.ecommerce.domain.valueobject;

/**
 * Las 4 categorías del catálogo definidas en el SyRS (mockup IU-01 y
 * Sección 1.1): Facial, Corporal, Capilar y Maquillaje. No es una entidad
 * gestionable con su propio CRUD porque RF-02 no lo pide.
 */
public enum CategoriaArticulo {
    FACIAL,
    CORPORAL,
    CAPILAR,
    MAQUILLAJE
}
