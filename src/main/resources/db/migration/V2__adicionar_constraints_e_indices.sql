CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_login_lower ON usuario (LOWER(login));
CREATE INDEX IF NOT EXISTS idx_sessao_data_horario ON sessao (data, horario);
CREATE INDEX IF NOT EXISTS idx_sessao_status ON sessao (status);
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_perfil CHECK (perfil IN ($$ADMIN$$, $$GESTOR$$));
ALTER TABLE sessao ADD CONSTRAINT ck_sessao_status CHECK (status IN ($$ATIVA$$, $$CANCELADA$$));
