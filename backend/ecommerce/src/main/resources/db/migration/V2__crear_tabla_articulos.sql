-- F-02: Gestion del catalogo de productos (CRUD).
-- CategoriaArticulo del dominio: FACIAL, CORPORAL, CAPILAR, MAQUILLAJE.

CREATE SEQUENCE articulos_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE articulos (
    id           NUMBER(19)      DEFAULT articulos_seq.NEXTVAL PRIMARY KEY,
    nombre       VARCHAR2(150)   NOT NULL,
    descripcion  VARCHAR2(1000)  NOT NULL,
    precio       NUMBER(12,2)    NOT NULL,
    categoria    VARCHAR2(20)    NOT NULL,
    marca        VARCHAR2(100),
    imagen_url   VARCHAR2(500)   NOT NULL,
    stock        NUMBER(10)      DEFAULT 0 NOT NULL,
    CONSTRAINT uq_articulos_nombre UNIQUE (nombre),
    CONSTRAINT ck_articulos_precio CHECK (precio > 0),
    CONSTRAINT ck_articulos_stock CHECK (stock >= 0),
    CONSTRAINT ck_articulos_categoria CHECK (categoria IN ('FACIAL', 'CORPORAL', 'CAPILAR', 'MAQUILLAJE'))
);
