CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO usuario (nome, login, senha_hash, perfil, ativo)
SELECT $$Administrador de Desenvolvimento$$, $$admin@localhost$$, crypt($$Admin@123$$, gen_salt($$bf$$, 12)), $$ADMIN$$, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario);

WITH calendario AS (
    SELECT
        dia::DATE AS data,
        ROW_NUMBER() OVER (ORDER BY dia)::INTEGER AS ordem
    FROM generate_series(
        DATE $$2026-01-01$$,
        DATE $$2026-12-31$$,
        INTERVAL $$7 days$$
    ) AS dia
)
INSERT INTO sessao (
    turma,
    data,
    horario,
    procurador,
    sala,
    status,
    motivo_cancelamento
)
SELECT
    (ARRAY[
        $$1a TESP$$, $$2a TESP$$, $$3a TESP$$, $$4a TESP$$,
        $$5a TESP$$, $$6a TESP$$, $$7a TESP$$, $$8a TESP$$,
        $$1a SESP$$, $$2a SESP$$, $$3a SESP$$, $$PLENO$$
    ])[((ordem - 1) % 12) + 1],
    data,
    (TIME $$09:00$$ + ((ordem - 1) % 7) * INTERVAL $$1 hour$$)::TIME,
    $$PROCURADOR DE DESENVOLVIMENTO $$ || LPAD((((ordem - 1) % 10) + 1)::TEXT, 2, $$0$$),
    (ARRAY[
        $$3o andar$$,
        $$5o andar, sala 1$$,
        $$5o andar, sala 2$$,
        $$7o andar, sala 1$$,
        $$7o andar, sala 2$$,
        $$9o andar, sala 1$$,
        $$9o andar, sala 2$$
    ])[((ordem - 1) % 7) + 1],
    CASE WHEN ordem % 10 = 0 THEN $$CANCELADA$$ ELSE $$ATIVA$$ END,
    CASE
        WHEN ordem % 10 = 0 THEN $$Sessao ficticia cancelada para testes.$$
        ELSE $$$$
    END
FROM calendario
WHERE NOT EXISTS (SELECT 1 FROM sessao);
