-- F-07: Gestion de estados del pedido (RF-07.1 a RF-07.8). Fechas en UTC.

CREATE SEQUENCE historial_estados_pedido_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- RF-07.7: cada cambio de estado deja una fila: fecha, estado anterior, nuevo,
-- quien lo hizo y, si es una cancelacion, el motivo (RF-07.5).
CREATE TABLE historial_estados_pedido (
    id               NUMBER(19)     DEFAULT historial_estados_pedido_seq.NEXTVAL PRIMARY KEY,
    pedido_id        NUMBER(19)     NOT NULL,
    -- NULL solo en la primera fila: el registro del pedido.
    estado_anterior  VARCHAR2(20),
    estado_nuevo     VARCHAR2(20)   NOT NULL,
    fecha            TIMESTAMP      NOT NULL,
    usuario_id       NUMBER(19)     NOT NULL,
    motivo           VARCHAR2(300),
    CONSTRAINT fk_historial_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT ck_historial_estado_nuevo CHECK (estado_nuevo IN
        ('PENDIENTE', 'CONFIRMADO', 'EN_PROCESO', 'EN_CAMINO', 'ENTREGADO', 'COMPLETADO', 'CANCELADO')),
    -- RF-07.5: no hay cancelacion sin motivo.
    CONSTRAINT ck_historial_motivo CHECK (estado_nuevo <> 'CANCELADO' OR motivo IS NOT NULL)
);

CREATE INDEX ix_historial_pedido ON historial_estados_pedido (pedido_id);
-- RF-07.1: la lista del personal filtra por estado y ordena por fecha.
CREATE INDEX ix_pedidos_estado_fecha ON pedidos (estado, creado_en);

-- Los pedidos registrados antes de F-07 reciben su primera fila: el registro
-- hecho por la clienta.
INSERT INTO historial_estados_pedido (pedido_id, estado_anterior, estado_nuevo, fecha, usuario_id, motivo)
SELECT id, NULL, 'PENDIENTE', creado_en, cliente_id, NULL FROM pedidos;
