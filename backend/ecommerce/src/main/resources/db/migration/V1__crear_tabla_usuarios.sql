-- F-01: Autenticacion y gestion de roles.
-- RolUsuario del dominio: CLIENTE, ASESORA_VENTAS, ENCARGADA_INVENTARIO, ADMINISTRADORA.

CREATE SEQUENCE usuarios_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- activo es BOOLEAN nativo (soportado desde Oracle 23ai): Hibernate 7 con
-- OracleDialect mapea el campo "boolean" de Java a este tipo por defecto.
CREATE TABLE usuarios (
    id               NUMBER(19)      DEFAULT usuarios_seq.NEXTVAL PRIMARY KEY,
    nombre           VARCHAR2(150)   NOT NULL,
    email            VARCHAR2(180)   NOT NULL,
    contrasena_hash  VARCHAR2(255)   NOT NULL,
    rol              VARCHAR2(30)    NOT NULL,
    activo           BOOLEAN         DEFAULT TRUE NOT NULL,
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('CLIENTE', 'ASESORA_VENTAS', 'ENCARGADA_INVENTARIO', 'ADMINISTRADORA'))
);
