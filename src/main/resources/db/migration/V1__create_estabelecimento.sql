CREATE TABLE estabelecimento(
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    documento VARCHAR(20),
    tipo_negocio VARCHAR(50) NOT NULL,
    latitude NUMERIC (10, 8) NOT NULL,
    longitude NUMERIC (11, 8) NOT NULL,
    endereco VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL

);

CREATE INDEX idx_estabelecimento_localizacao ON estabelecimento(latitude, longitude);