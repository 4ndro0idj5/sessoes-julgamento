DROP INDEX IF EXISTS idx_sessao_data_horario;

CREATE TABLE sessao_presencial (
    id BIGINT PRIMARY KEY REFERENCES sessao(id) ON DELETE CASCADE,
    data DATE NOT NULL,
    horario TIME NOT NULL,
    local VARCHAR(255) NOT NULL,
    procurador VARCHAR(255) NOT NULL
);

INSERT INTO sessao_presencial (id, data, horario, local, procurador)
SELECT id, data, horario, sala, procurador
FROM sessao;

CREATE TABLE sessao_virtual (
    id BIGINT PRIMARY KEY REFERENCES sessao(id) ON DELETE CASCADE,
    data_inicial DATE NOT NULL,
    data_final DATE NOT NULL,
    CONSTRAINT ck_sessao_virtual_periodo CHECK (data_final >= data_inicial)
);

ALTER TABLE sessao
    DROP COLUMN data,
    DROP COLUMN horario,
    DROP COLUMN sala,
    DROP COLUMN procurador;

CREATE INDEX idx_sessao_presencial_data_horario ON sessao_presencial (data, horario);
CREATE INDEX idx_sessao_virtual_periodo ON sessao_virtual (data_inicial, data_final);