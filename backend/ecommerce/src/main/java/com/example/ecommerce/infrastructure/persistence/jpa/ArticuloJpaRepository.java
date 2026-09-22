package com.example.ecommerce.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticuloJpaRepository extends JpaRepository<ArticuloJpaEntity, Long> {
    Optional<ArticuloJpaEntity> findByNombre(String nombre);
}
