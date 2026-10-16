package com.example.ecommerce.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticuloJpaRepository extends JpaRepository<ArticuloJpaEntity, Long> {

    /** RF-02.3: búsqueda por la clave normalizada del nombre (columna NOMBRE_CLAVE). */
    Optional<ArticuloJpaEntity> findByNombreClave(String nombreClave);
}
