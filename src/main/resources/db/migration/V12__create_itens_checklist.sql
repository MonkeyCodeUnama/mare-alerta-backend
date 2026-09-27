-- V12: Criar schema e indices para itens de checklist do comerciante

CREATE TABLE itens_checklist (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento(id) ON DELETE CASCADE,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    categoria VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    prioridade VARCHAR(50) NOT NULL DEFAULT 'MEDIA',
    ordem INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices B-Tree para buscas e filtros frequentes do frontend
CREATE INDEX idx_checklist_estabelecimento_id ON itens_checklist(estabelecimento_id);
CREATE INDEX idx_checklist_estab_status ON itens_checklist(estabelecimento_id, status);
CREATE INDEX idx_checklist_prioridade ON itens_checklist(prioridade);
