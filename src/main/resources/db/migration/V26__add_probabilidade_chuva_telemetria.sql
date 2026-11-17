ALTER TABLE leituras_telemetria
    ADD COLUMN IF NOT EXISTS probabilidade_chuva INTEGER;

ALTER TABLE leituras_telemetria
    ADD CONSTRAINT chk_telemetria_probabilidade_chuva
    CHECK (probabilidade_chuva IS NULL OR (probabilidade_chuva >= 0 AND probabilidade_chuva <= 100));
