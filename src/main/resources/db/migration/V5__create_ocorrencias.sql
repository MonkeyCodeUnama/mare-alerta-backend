CREATE TABLE ocorrencias(
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT NOT NULL,
    latitude NUMERIC(10, 8) NOT NULL,
    longitude NUMERIC(11, 8) NOT NULL,
    nivel_alagamento VARCHAR(30) NOT NULL,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL
);

CREATE INDEX idx_ocorrencias_coordenadas ON ocorrencias(latitude, longitude);
CREATE INDEX idx_ocorrencias_data_inicio ON ocorrencias(data_inicio DESC);