DROP INDEX IF EXISTS idx_ocorrencias_data_inicio;
ALTER TABLE ocorrencias DROP COLUMN IF EXISTS data_inicio;
ALTER TABLE ocorrencias DROP COLUMN IF EXISTS data_fim;

DROP INDEX IF EXISTS idx_estabelecimento_bairro;
DROP INDEX IF EXISTS idx_ocorrencias_estabelecimento_id;
DROP INDEX IF EXISTS idx_checklist_estabelecimento_id;

CREATE INDEX idx_ocorrencias_status_data ON ocorrencias(status, data_ocorrencia DESC);
CREATE INDEX idx_checklist_estab_ordem ON itens_checklist(estabelecimento_id, ordem);

