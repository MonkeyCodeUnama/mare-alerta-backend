-- V11: Migrar estabelecimento para endereco estruturado e remover coordenadas

-- Remover indice antigo baseado em coordenadas
DROP INDEX IF EXISTS idx_estabelecimento_localizacao;

-- Remover colunas antigas e adicionar endereco estruturado e campos cadastrais
ALTER TABLE estabelecimento
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS longitude,
    DROP COLUMN IF EXISTS endereco,
    ADD COLUMN responsavel VARCHAR(150) NOT NULL DEFAULT 'Não informado',
    ADD COLUMN bairro VARCHAR(100) NOT NULL DEFAULT 'Marco',
    ADD COLUMN logradouro VARCHAR(255) NOT NULL DEFAULT 'Não informado',
    ADD COLUMN numero VARCHAR(20) NOT NULL DEFAULT 'S/N',
    ADD COLUMN complemento VARCHAR(100),
    ADD COLUMN cep VARCHAR(10),
    ADD COLUMN municipio VARCHAR(100) NOT NULL DEFAULT 'Belém',
    ADD COLUMN uf VARCHAR(2) NOT NULL DEFAULT 'PA',
    ADD COLUMN telefone VARCHAR(20),
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Índices B-Tree para buscas otimizadas
CREATE INDEX idx_estabelecimento_bairro ON estabelecimento(bairro);
CREATE INDEX idx_estabelecimento_bairro_tipo ON estabelecimento(bairro, tipo_negocio);
CREATE INDEX idx_estabelecimento_ativo ON estabelecimento(ativo) WHERE deleted_at IS NULL;
