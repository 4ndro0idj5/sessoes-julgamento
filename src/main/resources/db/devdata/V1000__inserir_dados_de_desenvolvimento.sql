CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO usuario (nome, login, senha_hash, perfil, ativo)
SELECT 'Administrador de Desenvolvimento', 'admin@localhost', crypt('Admin@123', gen_salt('bf', 12)), 'ADMIN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario);

DO $dev$
DECLARE
    registro RECORD;
    sessao_id BIGINT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sessao) THEN
        FOR registro IN
            WITH calendario AS (
                SELECT dia::DATE AS data, ROW_NUMBER() OVER (ORDER BY dia)::INTEGER AS ordem
                FROM generate_series(DATE '2026-01-01', DATE '2026-12-31', INTERVAL '7 days') AS dia
            )
            SELECT
                (ARRAY[
                    '1ª TESP', '2ª TESP', '3ª TESP', '4ª TESP',
                    '5ª TESP', '6ª TESP', '7ª TESP', '8ª TESP',
                    '1ª SESP', '2ª SESP', '3ª SESP', 'PLENO'
                ])[((ordem - 1) % 12) + 1] AS turma,
                data,
                (TIME '09:00' + ((ordem - 1) % 7) * INTERVAL '1 hour')::TIME AS horario,
                'PROCURADOR DE DESENVOLVIMENTO ' || LPAD((((ordem - 1) % 10) + 1)::TEXT, 2, '0') AS procurador,
                (ARRAY[
                    '3º andar', '5º andar, sala 1', '5º andar, sala 2',
                    '7º andar, sala 1', '7º andar, sala 2',
                    '9º andar, sala 1', '9º andar, sala 2'
                ])[((ordem - 1) % 7) + 1] AS local,
                CASE WHEN ordem % 10 = 0 THEN 'CANCELADA' ELSE 'ATIVA' END AS status,
                CASE WHEN ordem % 10 = 0 THEN 'Sessao ficticia cancelada para testes.' ELSE '' END AS motivo
            FROM calendario
        LOOP
            INSERT INTO sessao (turma, status, motivo_cancelamento, pauta_ordinaria, aditamentos, pauta_mesa, preferencias)
            VALUES (registro.turma, registro.status, registro.motivo, '', '', '', '')
            RETURNING id INTO sessao_id;

            INSERT INTO sessao_presencial (id, data, horario, local, procurador)
            VALUES (sessao_id, registro.data, registro.horario, registro.local, registro.procurador);
        END LOOP;
    END IF;
END
$dev$;