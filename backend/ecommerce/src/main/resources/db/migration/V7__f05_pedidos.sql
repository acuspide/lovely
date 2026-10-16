-- F-05: Registro de pedidos (RF-05.1 a RF-05.7). Fechas en UTC.

-- RF-05.2: consecutivo del numero de pedido (LG-000001, LG-000002...).
-- NOCACHE para que no se salten numeros al reiniciar la base de datos.
CREATE SEQUENCE pedidos_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE detalles_pedido_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE pedidos (
    id                  NUMBER(19)     DEFAULT pedidos_seq.NEXTVAL PRIMARY KEY,
    -- RN21: un pedido pertenece a exactamente una clienta registrada.
    cliente_id          NUMBER(19)     NOT NULL,
    -- RF-05.1: estado inicial PENDIENTE. Se aceptan todos los estados del
    -- diagrama del SyRS (Figura 13); sus transiciones son F-07.
    estado              VARCHAR2(20)   NOT NULL,
    creado_en           TIMESTAMP      NOT NULL,
    -- RF-05.7: identificador del intento de compra, para no duplicar pedidos.
    clave_idempotencia  VARCHAR2(36)   NOT NULL,
    -- RF-04.6: datos de entrega.
    tipo_entrega        VARCHAR2(20)   NOT NULL,
    nombre_contacto     VARCHAR2(120)  NOT NULL,
    telefono_contacto   VARCHAR2(10)   NOT NULL,
    direccion           VARCHAR2(200),
    ciudad              VARCHAR2(80),
    indicaciones        VARCHAR2(300),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id),
    CONSTRAINT uq_pedidos_idempotencia UNIQUE (cliente_id, clave_idempotencia),
    CONSTRAINT ck_pedidos_estado CHECK (estado IN
        ('PENDIENTE', 'CONFIRMADO', 'EN_PROCESO', 'EN_CAMINO', 'ENTREGADO', 'COMPLETADO', 'CANCELADO')),
    CONSTRAINT ck_pedidos_tipo_entrega CHECK (tipo_entrega IN ('RETIRO_EN_TIENDA', 'DOMICILIO', 'ENVIO')),
    CONSTRAINT ck_pedidos_direccion CHECK (
        tipo_entrega = 'RETIRO_EN_TIENDA' OR (direccion IS NOT NULL AND ciudad IS NOT NULL))
);

CREATE INDEX ix_pedidos_cliente ON pedidos (cliente_id);

-- RF-05.3: cada linea guarda el nombre y el precio del producto al momento de
-- la compra. articulo_id no es llave foranea a proposito: un producto se puede
-- eliminar si solo aparece en pedidos cancelados (RF-02.9), y el pedido debe
-- conservar su historial.
CREATE TABLE detalles_pedido (
    id               NUMBER(19)     DEFAULT detalles_pedido_seq.NEXTVAL PRIMARY KEY,
    pedido_id        NUMBER(19)     NOT NULL,
    articulo_id      NUMBER(19)     NOT NULL,
    nombre_articulo  VARCHAR2(150)  NOT NULL,
    cantidad         NUMBER(10)     NOT NULL,
    precio_unitario  NUMBER(12,2)   NOT NULL,
    CONSTRAINT fk_detalles_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id) ON DELETE CASCADE,
    CONSTRAINT ck_detalles_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalles_precio CHECK (precio_unitario > 0)
);

CREATE INDEX ix_detalles_pedido ON detalles_pedido (pedido_id);
-- RF-02.9: consulta "¿este producto tiene pedidos activos?".
CREATE INDEX ix_detalles_articulo ON detalles_pedido (articulo_id);
