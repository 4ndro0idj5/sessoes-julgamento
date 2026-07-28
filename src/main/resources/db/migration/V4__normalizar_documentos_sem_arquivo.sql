UPDATE sessao
SET pauta_ordinaria = COALESCE(pauta_ordinaria, ''),
    aditamentos = COALESCE(aditamentos, ''),
    pauta_mesa = COALESCE(pauta_mesa, ''),
    preferencias = COALESCE(preferencias, '')
WHERE pauta_ordinaria IS NULL
   OR aditamentos IS NULL
   OR pauta_mesa IS NULL
   OR preferencias IS NULL;