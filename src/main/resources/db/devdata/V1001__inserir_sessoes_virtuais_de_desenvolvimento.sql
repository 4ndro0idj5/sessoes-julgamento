DO $dev$
DECLARE
    registro RECORD;
    sessao_id BIGINT;
BEGIN
    FOR registro IN
        SELECT *
        FROM (VALUES
            ('1ª TESP', DATE '2026-07-27', DATE '2026-07-31'),
            ('3ª TESP', DATE '2026-08-03', DATE '2026-08-07'),
            ('5ª TESP', DATE '2026-08-13', DATE '2026-08-20'),
            ('7ª TESP', DATE '2026-08-24', DATE '2026-08-28'),
            ('PLENO', DATE '2026-09-01', DATE '2026-09-04')
        ) AS dados(turma, data_inicial, data_final)
    LOOP
        IF NOT EXISTS (
            SELECT 1
            FROM sessao s
            JOIN sessao_virtual sv ON sv.id = s.id
            WHERE s.turma = registro.turma
              AND sv.data_inicial = registro.data_inicial
              AND sv.data_final = registro.data_final
        ) THEN
            INSERT INTO sessao (
                turma, status, motivo_cancelamento,
                pauta_ordinaria, aditamentos, pauta_mesa, preferencias
            )
            VALUES (registro.turma, 'ATIVA', '', '', '', '', '')
            RETURNING id INTO sessao_id;

            INSERT INTO sessao_virtual (id, data_inicial, data_final)
            VALUES (sessao_id, registro.data_inicial, registro.data_final);
        END IF;
    END LOOP;
END
$dev$;