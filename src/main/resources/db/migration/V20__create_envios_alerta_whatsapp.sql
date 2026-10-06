CREATE TABLE envios_alerta_whatsapp (
    id BIGSERIAL PRIMARY KEY,
    alerta_id BIGINT NOT NULL REFERENCES alerta(id) ON DELETE CASCADE,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento(id) ON DELETE CASCADE,
    telefone_destinatario VARCHAR(20) NOT NULL,
    status_envio VARCHAR(30) NOT NULL,
    whatsapp_message_id VARCHAR(100),
    tentativas INTEGER NOT NULL,
    erro_mensagem TEXT,
    criado_em TIMESTAMPTZ NOT NULL,
    enviado_em TIMESTAMPTZ,
    CONSTRAINT chk_envio_whatsapp_status CHECK (status_envio IN ('PENDENTE', 'PROCESSANDO', 'ENVIADO', 'FALHA')),
    CONSTRAINT chk_envio_whatsapp_tentativas CHECK (tentativas >= 0)
);

CREATE INDEX idx_envio_whatsapp_status ON envios_alerta_whatsapp (status_envio, criado_em);
CREATE INDEX idx_envio_whatsapp_alerta ON envios_alerta_whatsapp (alerta_id);
CREATE INDEX idx_envio_whatsapp_estabelecimento ON envios_alerta_whatsapp (estabelecimento_id);
