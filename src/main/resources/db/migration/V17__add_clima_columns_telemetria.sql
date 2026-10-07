ALTER TABLE leituras_telemetria
    ADD COLUMN temperatura NUMERIC(4, 1),
    ADD COLUMN vento_velocidade NUMERIC(5, 2),
    ADD COLUMN vento_direcao NUMERIC(5, 2);
