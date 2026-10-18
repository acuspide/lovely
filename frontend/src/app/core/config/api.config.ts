/**
 * Dirección del backend. En un proyecto más grande esto viviría en los
 * environments de Angular (dev/prod); aquí basta una constante única porque
 * el proyecto solo se ejecuta contra el backend local durante el desarrollo.
 */
export const SERVIDOR_URL = 'http://localhost:8080';

/** URL base de la API REST. */
export const API_BASE_URL = `${SERVIDOR_URL}/api`;
