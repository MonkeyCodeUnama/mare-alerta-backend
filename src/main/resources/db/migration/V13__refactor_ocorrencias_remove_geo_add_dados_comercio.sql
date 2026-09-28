DROP INDEX IF EXISTS idx_ocorrencias_coordenadas;

ALTER TABLE ocorrencias
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS longitude,
    ALTER COLUMN nivel_alagamento DROP NOT NULL,
    ALTER COLUMN data_inicio DROP NOT NULL,
    ALTER COLUMN descricao DROP NOT NULL,
    ADD COLUMN protocolo VARCHAR(50),
    ADD COLUMN estabelecimento_id BIGINT REFERENCES estabelecimento(id) ON DELETE SET NULL,
    ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'RASCUNHO',
    ADD COLUMN data_ocorrencia DATE NOT NULL DEFAULT CURRENT_DATE,
    ADD COLUMN hora_inicio TIME NOT NULL DEFAULT '00:00:00',
    ADD COLUMN hora_fim TIME,
    ADD COLUMN faixa_lamina_agua VARCHAR(50),
    ADD COLUMN categorias_dano TEXT,
    ADD COLUMN valor_estimado_prejuizo NUMERIC(12, 2),
    ADD COLUMN horas_fechado INT,
    ADD COLUMN tamanho_equipe INT,
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

UPDATE ocorrencias SET protocolo = 'OC-' || id WHERE protocolo IS NULL;
ALTER TABLE ocorrencias ALTER COLUMN protocolo SET NOT NULL;
ALTER TABLE ocorrencias ADD CONSTRAINT uq_ocorrencias_protocolo UNIQUE (protocolo);

CREATE INDEX idx_ocorrencias_estabelecimento_id ON ocorrencias(estabelecimento_id);
CREATE INDEX idx_ocorrencias_estab_data ON ocorrencias(estabelecimento_id, data_ocorrencia DESC);
CREATE INDEX idx_ocorrencias_status ON ocorrencias(status);
CREATE INDEX idx_ocorrencias_data ON ocorrencias(data_ocorrencia DESC);
