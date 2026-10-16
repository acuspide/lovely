-- F-01: Autenticacion y gestion de roles (RF-01.1 a RF-01.8).
-- Todas las fechas se guardan en UTC.

-- RF-01.1: telefono y constancia de aceptacion de Habeas Data (Ley 1581 de 2012).
--   El telefono queda opcional en la tabla porque es opcional para el personal
--   y las cuentas creadas antes de esta version no lo tienen; para las clientas
--   lo exige el dominio.
-- RF-01.4: contador de intentos fallidos y fin del bloqueo temporal.
ALTER TABLE usuarios ADD (
    telefono                  VARCHAR2(10),
    intentos_fallidos         NUMBER(2)     DEFAULT 0 NOT NULL,
    bloqueado_hasta           TIMESTAMP,
    habeas_data_aceptado_en   TIMESTAMP,
    CONSTRAINT ck_usuarios_intentos CHECK (intentos_fallidos >= 0)
);

-- RF-01.6 / RF-01.7: sesiones del lado del servidor, para poder vencerlas por
-- inactividad y anularlas al cerrar sesion.
CREATE TABLE sesiones (
    id                VARCHAR2(36)   PRIMARY KEY,
    usuario_id        NUMBER(19)     NOT NULL,
    iniciada_en       TIMESTAMP      NOT NULL,
    ultima_actividad  TIMESTAMP      NOT NULL,
    cerrada_en        TIMESTAMP,
    CONSTRAINT fk_sesiones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX ix_sesiones_usuario ON sesiones (usuario_id);

-- RF-01.8: enlaces de recuperacion de contrasena (un solo uso, 30 minutos).
--   Solo se guarda la huella SHA-256 del token, nunca el token.
CREATE SEQUENCE tokens_recuperacion_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE tokens_recuperacion (
    id          NUMBER(19)     DEFAULT tokens_recuperacion_seq.NEXTVAL PRIMARY KEY,
    usuario_id  NUMBER(19)     NOT NULL,
    huella      VARCHAR2(64)   NOT NULL,
    creado_en   TIMESTAMP      NOT NULL,
    expira_en   TIMESTAMP      NOT NULL,
    usado_en    TIMESTAMP,
    CONSTRAINT uq_tokens_recuperacion_huella UNIQUE (huella),
    CONSTRAINT fk_tokens_recuperacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX ix_tokens_recuperacion_usuario ON tokens_recuperacion (usuario_id);
