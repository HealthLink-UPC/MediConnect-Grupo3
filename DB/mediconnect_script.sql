-- =====================================================
-- MEDICONNECT - Script PostgreSQL (pgAdmin)
-- =====================================================

CREATE SCHEMA IF NOT EXISTS mediconnect;

CREATE TABLE mediconnect.tm_rol (
    id_rol      BIGSERIAL PRIMARY KEY,
    nombre_rol  VARCHAR(100) NOT NULL,
    estado_rol  INTEGER NOT NULL
);

CREATE TABLE mediconnect.tm_usuario (
    id_usuario       BIGSERIAL PRIMARY KEY,
    id_rol           BIGINT NOT NULL,
    correo_usuario   VARCHAR(100) NOT NULL UNIQUE,
    clave_usuario    VARCHAR(100) NOT NULL,
    nombre_usuario   VARCHAR(100) NOT NULL,
    apellido_usuario VARCHAR(100) NOT NULL,
    dni              VARCHAR(8)   NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    telefono         VARCHAR(9)   NOT NULL,
    estado_usuario   INTEGER NOT NULL,
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol) REFERENCES mediconnect.tm_rol (id_rol)
);

CREATE TABLE mediconnect.tm_auditoria (
    id_auditoria      BIGSERIAL PRIMARY KEY,
    id_creacion        BIGINT NOT NULL,
    id_edicion        BIGINT NULL,
    id_eliminacion    BIGINT NULL,
    id_registro      BIGINT NOT NULL,
    fecha_creacion   TIMESTAMP NOT NULL,
    fecha_edicion     TIMESTAMP NULL,
    fecha_eliminacion TIMESTAMP NULL,
    tabla_afectada    VARCHAR(50) NOT NULL,
    estado_auditoria  INTEGER NOT NULL,
    CONSTRAINT fk_aud_creacion
        FOREIGN KEY (id_creacion)     REFERENCES mediconnect.tm_usuario (id_usuario),
    CONSTRAINT fk_aud_edicion
        FOREIGN KEY (id_edicion)     REFERENCES mediconnect.tm_usuario (id_usuario),
    CONSTRAINT fk_aud_eliminacion
        FOREIGN KEY (id_eliminacion) REFERENCES mediconnect.tm_usuario (id_usuario),
    CONSTRAINT uq_aud_registro UNIQUE (tabla_afectada, id_registro)
);

CREATE TABLE mediconnect.tm_medico (
    id_medico        BIGSERIAL PRIMARY KEY,
    id_usuario       BIGINT NOT NULL UNIQUE,
    especialidad     VARCHAR(100) NOT NULL,
    cmp              VARCHAR(10)  NOT NULL UNIQUE,
    centro_atencion  VARCHAR(100) NOT NULL,
    estado_medico    INTEGER NOT NULL,
    CONSTRAINT fk_medico_usuario
        FOREIGN KEY (id_usuario) REFERENCES mediconnect.tm_usuario (id_usuario)
);

CREATE TABLE mediconnect.tm_paciente (
    id_paciente     BIGSERIAL PRIMARY KEY,
    id_usuario      BIGINT NOT NULL UNIQUE,
    tipo_sangre     VARCHAR(50)      NOT NULL,
    peso            DOUBLE PRECISION NOT NULL,
    talla           DOUBLE PRECISION NOT NULL,
    estado_paciente INTEGER NOT NULL,
    CONSTRAINT fk_paciente_usuario
        FOREIGN KEY (id_usuario) REFERENCES mediconnect.tm_usuario (id_usuario)
);

CREATE TABLE mediconnect.tm_enfermedad (
    id_enfermedad     BIGSERIAL PRIMARY KEY,
    nombre_enfermedad VARCHAR(100) NOT NULL,
    estado_enfermedad INTEGER NOT NULL
);

CREATE TABLE mediconnect.tm_alergia (
    id_alergia     BIGSERIAL PRIMARY KEY,
    nombre_alergia VARCHAR(100) NOT NULL,
    estado_alergia INTEGER NOT NULL
);

CREATE TABLE mediconnect.tm_medicamento (
    id_medicamento     BIGSERIAL PRIMARY KEY,
    nombre_medicamento VARCHAR(100) NOT NULL,
    concentracion      VARCHAR(50)  NOT NULL,
    estado_medicamento INTEGER NOT NULL
);

CREATE TABLE mediconnect.tm_diagnostico (
    id_diagnostico     BIGSERIAL PRIMARY KEY,
    id_enfermedad      BIGINT NOT NULL,
    id_paciente        BIGINT NOT NULL,
    estado_diagnostico INTEGER NOT NULL,
    CONSTRAINT fk_diag_enfermedad
        FOREIGN KEY (id_enfermedad) REFERENCES mediconnect.tm_enfermedad (id_enfermedad),
    CONSTRAINT fk_diag_paciente
        FOREIGN KEY (id_paciente)   REFERENCES mediconnect.tm_paciente (id_paciente),
    -- Un paciente no puede tener la misma enfermedad diagnosticada dos veces
    CONSTRAINT uq_diag_paciente_enfermedad
        UNIQUE (id_paciente, id_enfermedad)
);

CREATE TABLE mediconnect.tm_sensibilidad (
    id_sensibilidad     BIGSERIAL PRIMARY KEY,
    id_paciente         BIGINT NOT NULL,
    id_alergia          BIGINT NOT NULL,
    estado_sensibilidad INTEGER NOT NULL,
    CONSTRAINT fk_sens_paciente
        FOREIGN KEY (id_paciente) REFERENCES mediconnect.tm_paciente (id_paciente),
    CONSTRAINT fk_sens_alergia
        FOREIGN KEY (id_alergia)  REFERENCES mediconnect.tm_alergia (id_alergia),
    -- Un paciente no puede tener la misma alergia registrada dos veces
    CONSTRAINT uq_sens_paciente_alergia
        UNIQUE (id_paciente, id_alergia)
);

CREATE TABLE mediconnect.tm_receta (
    id_receta            BIGSERIAL PRIMARY KEY,
    id_paciente          BIGINT NOT NULL,
    id_medico            BIGINT NOT NULL,
    fecha_emision        DATE NOT NULL,
    fecha_vencimiento    DATE NOT NULL,
    codigo_verificacion  VARCHAR(50) NOT NULL UNIQUE,
    estado_receta        INTEGER NOT NULL,
    CONSTRAINT fk_receta_paciente
        FOREIGN KEY (id_paciente) REFERENCES mediconnect.tm_paciente (id_paciente),
    CONSTRAINT fk_receta_medico
        FOREIGN KEY (id_medico)   REFERENCES mediconnect.tm_medico (id_medico)
);

CREATE TABLE mediconnect.tm_solicitud (
    id_solicitud     BIGSERIAL PRIMARY KEY,
    id_paciente      BIGINT NOT NULL,
    id_medico        BIGINT NOT NULL,
    id_receta        BIGINT NULL,
    tipo_solicitud   VARCHAR(50)  NOT NULL,
    motivo           VARCHAR(500) NOT NULL,
    fecha_solicitud  DATE NOT NULL,
    motivo_estado    VARCHAR(500) NULL,
    estado_solicitud INTEGER NOT NULL,
    CONSTRAINT fk_sol_paciente
        FOREIGN KEY (id_paciente) REFERENCES mediconnect.tm_paciente (id_paciente),
    CONSTRAINT fk_sol_medico
        FOREIGN KEY (id_medico)   REFERENCES mediconnect.tm_medico (id_medico),
    CONSTRAINT fk_sol_receta
        FOREIGN KEY (id_receta)   REFERENCES mediconnect.tm_receta (id_receta)
);

CREATE TABLE mediconnect.tm_pedido (
    id_pedido      BIGSERIAL PRIMARY KEY,
    id_solicitud   BIGINT NOT NULL,
    id_medicamento BIGINT NOT NULL,
    estado_pedido  INTEGER NOT NULL,
    CONSTRAINT fk_pedido_solicitud
        FOREIGN KEY (id_solicitud)   REFERENCES mediconnect.tm_solicitud (id_solicitud),
    CONSTRAINT fk_pedido_medicamento
        FOREIGN KEY (id_medicamento) REFERENCES mediconnect.tm_medicamento (id_medicamento),
    -- Una solicitud no puede pedir el mismo medicamento dos veces
    CONSTRAINT uq_pedido_solicitud_medicamento
        UNIQUE (id_solicitud, id_medicamento)
);

CREATE TABLE mediconnect.tm_tratamiento (
    id_tratamiento     BIGSERIAL PRIMARY KEY,
    id_receta          BIGINT NOT NULL,
    id_medicamento     BIGINT NOT NULL,
    dosis              VARCHAR(100) NOT NULL,
    frecuencia         VARCHAR(50)  NOT NULL,
    fecha_inicio       DATE NOT NULL,
    fecha_fin          DATE NOT NULL,
    estado_tratamiento INTEGER NOT NULL,
    CONSTRAINT fk_trat_receta
        FOREIGN KEY (id_receta)      REFERENCES mediconnect.tm_receta (id_receta),
    CONSTRAINT fk_trat_medicamento
        FOREIGN KEY (id_medicamento) REFERENCES mediconnect.tm_medicamento (id_medicamento),
    -- Una receta no puede tener el mismo medicamento dos veces
    CONSTRAINT uq_trat_receta_medicamento
        UNIQUE (id_receta, id_medicamento)
);
