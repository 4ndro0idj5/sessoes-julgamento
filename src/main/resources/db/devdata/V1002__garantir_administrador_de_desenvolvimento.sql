CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO usuario (nome, login, senha_hash, perfil, ativo)
VALUES (
    'Administrador de Desenvolvimento',
    'admin@localhost',
    crypt('Admin@123', gen_salt('bf', 12)),
    'ADMIN',
    TRUE
)
ON CONFLICT (login) DO UPDATE
SET nome = EXCLUDED.nome,
    senha_hash = EXCLUDED.senha_hash,
    perfil = EXCLUDED.perfil,
    ativo = EXCLUDED.ativo,
    atualizado_em = CURRENT_TIMESTAMP;