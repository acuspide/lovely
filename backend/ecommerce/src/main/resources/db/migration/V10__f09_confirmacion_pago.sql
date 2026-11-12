-- F-09: confirmacion del pago de un pedido (RN11, RF-09.1 a RF-09.6).
-- La version 1.0 no procesa pagos en linea: el personal (Asesora de ventas o
-- Administradora) verifica que el dinero llego y lo registra aqui. Solo
-- despues de esto el pedido pasa a Confirmado y se emite su factura.
-- Un pedido tiene a lo sumo un pago, por eso va en columnas del mismo pedido.

ALTER TABLE pedidos ADD (
    -- RF-09.2: TRANSFERENCIA o EFECTIVO.
    pago_medio            VARCHAR2(20),
    -- RF-09.2: numero de la transaccion; obligatorio en transferencias.
    pago_referencia       VARCHAR2(60),
    -- RF-09.3: igual al total del pedido (lo valida el dominio).
    pago_monto            NUMBER(12,2),
    -- RF-09.4: dia en que el dinero llego (hora de Colombia).
    pago_fecha_recepcion  DATE,
    -- RF-09.2: quien confirmo y cuando (UTC).
    pago_confirmado_por   NUMBER(19),
    pago_confirmado_en    TIMESTAMP
);

ALTER TABLE pedidos ADD CONSTRAINT fk_pedidos_pago_usuario
    FOREIGN KEY (pago_confirmado_por) REFERENCES usuarios (id);

ALTER TABLE pedidos ADD CONSTRAINT ck_pedidos_pago_medio
    CHECK (pago_medio IS NULL OR pago_medio IN ('TRANSFERENCIA', 'EFECTIVO'));

-- RF-09.2: los datos del pago se guardan todos juntos o ninguno, y una
-- transferencia siempre trae su referencia.
ALTER TABLE pedidos ADD CONSTRAINT ck_pedidos_pago_completo CHECK (
    (pago_medio IS NULL AND pago_monto IS NULL AND pago_fecha_recepcion IS NULL
        AND pago_confirmado_por IS NULL AND pago_confirmado_en IS NULL AND pago_referencia IS NULL)
    OR (pago_medio IS NOT NULL AND pago_monto > 0 AND pago_fecha_recepcion IS NOT NULL
        AND pago_confirmado_por IS NOT NULL AND pago_confirmado_en IS NOT NULL
        AND (pago_medio <> 'TRANSFERENCIA' OR pago_referencia IS NOT NULL))
);
