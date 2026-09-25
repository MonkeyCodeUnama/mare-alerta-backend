CREATE TABLE usuario(
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP NULL,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento(id)

);

CREATE INDEX idx_usuario_estabelecimento ON usuario(estabelecimento_id) WHERE deleted_at IS NULL