-- V15: Criar schema e indices para telemetria de maré e precipitação (estações fluviométricas / meteorológicas)

CREATE TABLE leituras_telemetria (
    id BIGSERIAL PRIMARY KEY,
    estacao_codigo VARCHAR(50) NOT NULL,
    estacao_nome VARCHAR(150),
    tipo_registro VARCHAR(50) NOT NULL,
    nivel_mare NUMERIC(5, 2),
    precipitacao NUMERIC(5, 2),
    data_hora TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Evitar dados duplicados para mesma estação, timestamp e tipo
CREATE UNIQUE INDEX uq_telemetria_estacao_data_tipo ON leituras_telemetria(estacao_codigo, data_hora, tipo_registro);

-- Índices B-Tree para consultas de séries temporais e dashboard
CREATE INDEX idx_telemetria_estacao_data ON leituras_telemetria(estacao_codigo, data_hora DESC);
CREATE INDEX idx_telemetria_tipo_data ON leituras_telemetria(tipo_registro, data_hora DESC);
CREATE INDEX idx_telemetria_data_hora ON leituras_telemetria(data_hora DESC);
