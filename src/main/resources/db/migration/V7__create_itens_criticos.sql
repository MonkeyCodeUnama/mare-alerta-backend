CREATE TABLE itens_criticos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento(id)
);

CREATE INDEX idx_itens_criticos_estabelecimento ON itens_criticos(estabelecimento_id);