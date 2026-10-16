-- F-02 / RF-02.3: no pueden existir dos productos con el mismo nombre, sin
-- importar mayusculas, tildes ni espacios repetidos ("Crema Hidratante" y
-- "crema  hidratánte" son el mismo producto).
--
-- Se agrega NOMBRE_CLAVE con el nombre normalizado (lo calcula el dominio en
-- NombreArticulo.clave()) y la restriccion UNIQUE pasa de NOMBRE a esa columna.

-- 1. Limpia espacios sobrantes de los nombres ya registrados.
UPDATE articulos
   SET nombre = REGEXP_REPLACE(TRIM(nombre), '\s+', ' ');

-- 2. Nueva columna con la clave de comparacion.
ALTER TABLE articulos ADD (nombre_clave VARCHAR2(150));

UPDATE articulos
   SET nombre_clave = LOWER(TRANSLATE(nombre,
         'ÁÉÍÓÚÀÈÌÒÙÄËÏÖÜÂÊÎÔÛÑÇáéíóúàèìòùäëïöüâêîôûñç',
         'AEIOUAEIOUAEIOUAEIOUNCaeiouaeiouaeiouaeiounc'));

ALTER TABLE articulos MODIFY (nombre_clave NOT NULL);

-- 3. La unicidad ahora se garantiza sobre la clave, no sobre el texto exacto.
ALTER TABLE articulos DROP CONSTRAINT uq_articulos_nombre;
ALTER TABLE articulos ADD CONSTRAINT uq_articulos_nombre_clave UNIQUE (nombre_clave);
