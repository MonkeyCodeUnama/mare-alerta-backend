CREATE TABLE previsoes_chuva_ml (
    id BIGSERIAL PRIMARY KEY,
    estacao_codigo VARCHAR(50) NOT NULL,
    instante_emissao TIMESTAMPTZ NOT NULL,
    instante_alvo TIMESTAMPTZ NOT NULL,
    horizonte_horas INTEGER NOT NULL,
    probabilidade_chuva DOUBLE PRECISION NOT NULL,
    chuva_esperada BOOLEAN NOT NULL,
    precipitacao_estimada_mm NUMERIC(6, 2),
    versao_modelo VARCHAR(50) NOT NULL,
    status_qualidade VARCHAR(30) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_previsao_ml_probabilidade CHECK (probabilidade_chuva >= 0.0 AND probabilidade_chuva <= 1.0),
    CONSTRAINT chk_previsao_ml_horizonte CHECK (horizonte_horas > 0),
    CONSTRAINT chk_previsao_ml_status CHECK (status_qualidade IN ('OK', 'DEGRADADO', 'DADOS_INSUFICIENTES', 'INDISPONIVEL')),
    CONSTRAINT uk_previsao_ml_emissao_alvo UNIQUE (estacao_codigo, versao_modelo, instante_emissao, instante_alvo)
);

CREATE INDEX idx_previsao_ml_alvo ON previsoes_chuva_ml (estacao_codigo, instante_alvo);
CREATE INDEX idx_previsao_ml_emissao ON previsoes_chuva_ml (estacao_codigo, instante_emissao DESC);

