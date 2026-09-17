CREATE UNIQUE INDEX idx_estabelecimento_documento_ativo
ON estabelecimento(documento)
WHERE deleted_at IS NULL AND documento IS NOT NULL;
