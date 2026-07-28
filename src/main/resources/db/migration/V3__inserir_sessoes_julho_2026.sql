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
    dados.turma,
    dados.data,
    dados.horario,
    dados.procurador,
    dados.sala,
    'ATIVA',
    ''
FROM (
    VALUES
        ('5ª TESP', DATE '2026-07-01', TIME '14:00', 'MAURÍCIO RIBEIRO MANSO', '5º andar, sala 2'),
        ('PLENO', DATE '2026-07-02', TIME '13:30', 'LEONARDO CARDOSO DE FREITAS', '3º andar'),
        ('3ª TESP', DATE '2026-07-07', TIME '14:00', 'ANDRÉ TERRIGNO BARBEITAS', '7º andar, sala 1'),
        ('7ª TESP', DATE '2026-07-08', TIME '14:00', 'LUIZ MENDES SIMÕES', '7º andar, sala 2'),
        ('8ª TESP', DATE '2026-07-08', TIME '14:00', 'BIANCA MATAL', '7º andar, sala 1'),
        ('1ª TESP', DATE '2026-07-09', TIME '13:30', 'ANA PADILHA LUCIANO DE OLIVEIRA', '7º andar, sala 2'),
        ('2ª TESP', DATE '2026-07-14', TIME '13:30', 'ANTÔNIO AUGUSTO SOARES CANEDO NETO', '9º andar, sala 2'),
        ('3ª TESP', DATE '2026-07-14', TIME '14:00', 'DENISE LORENA DUQUE ESTRADA', '7º andar, sala 1'),
        ('5ª TESP', DATE '2026-07-15', TIME '14:00', 'NEIDE MARA CAVALCANTI CARDOSO DE OLIVEIRA', '5º andar, sala 2'),
        ('2ª TESP', DATE '2026-07-16', TIME '13:30', 'LEONARDO LUIZ DE FIGUEIREDO COSTA', '7º andar, sala 2'),
        ('1ª TESP', DATE '2026-07-21', TIME '13:30', 'JOÃO AKIRA OMOTO', '9º andar, sala 1'),
        ('2ª TESP', DATE '2026-07-21', TIME '13:30', 'GINO AUGUSTO DE OLIVEIRA LICCIONE', '9º andar, sala 2'),
        ('3ª TESP', DATE '2026-07-21', TIME '14:00', 'VAGNER LEÃO DA COSTA', '7º andar, sala 1'),
        ('7ª TESP', DATE '2026-07-22', TIME '14:00', 'MARIA HELENA DE CARVALHO NOGUEIRA DE PAULA', '7º andar, sala 2'),
        ('1ª TESP', DATE '2026-07-28', TIME '13:30', 'MAURÍCIO ANDREIUOLO RODRIGUES', '9º andar, sala 1'),
        ('2ª TESP', DATE '2026-07-28', TIME '13:30', 'ZANI CAJUEIRO TOBIAS DE SOUZA', '9º andar, sala 2'),
        ('3ª TESP', DATE '2026-07-28', TIME '14:00', 'ADRIANA DE FARIAS PEREIRA', '7º andar, sala 1'),
        ('5ª TESP', DATE '2026-07-30', TIME '13:00', 'ANAIVA OBERST CORDOVIL', '5º andar, sala 2')
) AS dados(turma, data, horario, procurador, sala)
WHERE NOT EXISTS (
    SELECT 1
    FROM sessao existente
    WHERE existente.turma = dados.turma
      AND existente.data = dados.data
      AND existente.horario = dados.horario
);
