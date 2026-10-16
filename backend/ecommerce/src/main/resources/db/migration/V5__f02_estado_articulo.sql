-- F-02 / RF-02.7: un producto puede desactivarse para ocultarlo del catalogo
-- sin borrarlo (alternativa a eliminar cuando tiene pedidos, RF-02.9).
-- BOOLEAN nativo de Oracle 23ai, igual que USUARIOS.ACTIVO (V1).
-- Todos los productos existentes quedan activos.
ALTER TABLE articulos ADD (activo BOOLEAN DEFAULT TRUE NOT NULL);
