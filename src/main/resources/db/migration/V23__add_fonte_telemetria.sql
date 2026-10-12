ALTER TABLE leituras_telemetria ADD COLUMN fonte VARCHAR(30) NOT NULL DEFAULT 'OPEN_METEO';
DROP INDEX IF EXISTS uq_telemetria_estacao_data_tipo;
CREATE UNIQUE INDEX uq_telemetria_chave ON leituras_telemetria(estacao_codigo, fonte, tipo_registro, data_hora);
ALTER TABLE leituras_telemetria ADD CONSTRAINT chk_telemetria_fonte CHECK (fonte IN ('OPEN_METEO', 'TABUA_MARE'));

