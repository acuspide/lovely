-- F-08: Factura en PDF (RF-08.1 a RF-08.7). Fechas en UTC.

-- RF-08.2 / RN06: consecutivo del numero de factura (FV-000001, FV-000002...).
-- NOCACHE para que no se salten numeros al reiniciar la base de datos.
CREATE SEQUENCE facturas_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE facturas (
    id                NUMBER(19)     DEFAULT facturas_seq.NEXTVAL PRIMARY KEY,
    -- RN02 / RN15: cada factura pertenece a un unico pedido, y un pedido tiene
    -- como maximo una factura.
    pedido_id         NUMBER(19)     NOT NULL,
    emitida_en        TIMESTAMP      NOT NULL,
    -- RF-08.6: una factura no se borra; si el pedido se cancela queda ANULADA.
    estado            VARCHAR2(10)   NOT NULL,
    anulada_en        TIMESTAMP,
    -- RF-08.3: copia de los datos de la clienta y de la tasa de IVA del momento,
    -- para que el PDF salga siempre igual aunque la cuenta o la tasa cambien.
    nombre_clienta    VARCHAR2(120)  NOT NULL,
    email_clienta     VARCHAR2(180)  NOT NULL,
    telefono_clienta  VARCHAR2(10),
    tasa_iva          NUMBER(5,4)    NOT NULL,
    CONSTRAINT fk_facturas_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
    CONSTRAINT uq_facturas_pedido UNIQUE (pedido_id),
    CONSTRAINT ck_facturas_estado CHECK (estado IN ('EMITIDA', 'ANULADA')),
    CONSTRAINT ck_facturas_anulacion CHECK (estado = 'EMITIDA' OR anulada_en IS NOT NULL),
    CONSTRAINT ck_facturas_tasa_iva CHECK (tasa_iva >= 0 AND tasa_iva <= 1)
);
