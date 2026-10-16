-- F-02 / RF-02.4 / CU-01 curso alterno A4: si la carga de la imagen falla, la
-- Administradora puede guardar el producto sin imagen. IMAGEN_URL pasa a ser
-- opcional; el catalogo muestra "Sin imagen" cuando es NULL.
ALTER TABLE articulos MODIFY (imagen_url NULL);
