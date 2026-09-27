-- V14: Criar tabela de fotos anexas das ocorrencias

CREATE TABLE ocorrencia_fotos (
    id BIGSERIAL PRIMARY KEY,
    ocorrencia_id BIGINT NOT NULL REFERENCES ocorrencias(id) ON DELETE CASCADE,
    nome_arquivo VARCHAR(255) NOT NULL,
    url_foto VARCHAR(1000) NOT NULL,
    tamanho_bytes BIGINT,
    content_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Índice B-Tree para carregar fotos de uma ocorrencia rapidamente
CREATE INDEX idx_ocorrencia_fotos_ocorrencia_id ON ocorrencia_fotos(ocorrencia_id);
