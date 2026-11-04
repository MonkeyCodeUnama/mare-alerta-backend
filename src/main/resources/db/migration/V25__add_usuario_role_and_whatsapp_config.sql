ALTER TABLE usuario ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE usuario ADD CONSTRAINT chk_usuario_role CHECK (role IN ('USER', 'ADMIN'));

CREATE TABLE configuracao_whatsapp (
    id BIGSERIAL PRIMARY KEY,
    instancia VARCHAR(100) NOT NULL,
    telefone_remetente VARCHAR(30),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    enviar_alertas_automaticos BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uq_configuracao_whatsapp_instancia ON configuracao_whatsapp(instancia);
