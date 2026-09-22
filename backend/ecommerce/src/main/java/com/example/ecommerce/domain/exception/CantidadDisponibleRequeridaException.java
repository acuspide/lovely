package com.example.ecommerce.domain.exception;

public class CantidadDisponibleRequeridaException extends ReglaDominioException {
  public CantidadDisponibleRequeridaException() {
    super("El artículo debe indicar una cantidad disponible.");
  }
}
