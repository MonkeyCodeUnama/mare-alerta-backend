CREATE TABLE alerta (
    id BIGSERIAL PRIMARY KEY,
    nivel_risco VARCHAR(20) NOT NULL,
    mensagem TEXT NOT NULL,
    data_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo_alerta VARCHAR(30) NOT NULL,
    origem_alerta VARCHAR(20) NOT NULL DEFAULT 'AUTOMATICO',
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento(id) ON DELETE CASCADE
);

CREATE INDEX idx_alerta_estabelecimento ON alerta(estabelecimento_id, data_envio DESC)