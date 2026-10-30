ALTER TABLE estabelecimento
    ADD CONSTRAINT chk_estabelecimento_tipo_negocio
    CHECK (tipo_negocio IN ('MERCADO', 'RESTAURANTE', 'LANCHONETE', 'PADARIA', 'FARMACIA', 'SALAO_BELEZA', 'OFICINA', 'ALIMENTACAO', 'BELEZA', 'VAREJO', 'OUTROS')),
    ADD CONSTRAINT chk_estabelecimento_ativo_deleted
    CHECK (deleted_at IS NULL OR ativo = FALSE);

ALTER TABLE ocorrencias
    ADD CONSTRAINT chk_ocorrencias_status
    CHECK (status IN ('RASCUNHO', 'ENVIADA', 'EM_ANALISE', 'FINALIZADA')),
    ADD CONSTRAINT chk_ocorrencias_nivel_alagamento
    CHECK (nivel_alagamento IS NULL OR nivel_alagamento IN ('LEVE', 'MODERADO', 'GRAVE', 'CRITICO')),
    ADD CONSTRAINT chk_ocorrencias_valores_nao_negativos
    CHECK (
        (valor_estimado_prejuizo IS NULL OR valor_estimado_prejuizo >= 0) AND
        (horas_fechado IS NULL OR horas_fechado >= 0) AND
        (tamanho_equipe IS NULL OR tamanho_equipe >= 0)
    );

ALTER TABLE itens_checklist
    ADD CONSTRAINT chk_checklist_prioridade
    CHECK (prioridade IN ('ALTA', 'MEDIA', 'BAIXA')),
    ADD CONSTRAINT chk_checklist_status
    CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDO', 'NAO_APLICAVEL'));

ALTER TABLE alerta
    ADD CONSTRAINT chk_alerta_nivel_risco
    CHECK (nivel_risco IN ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO')),
    ADD CONSTRAINT chk_alerta_tipo
    CHECK (tipo_alerta IN ('ALAGAMENTO', 'MARE_ALTA', 'TEMPESTADE', 'INUNDACAO', 'PREVENTIVO')),
    ADD CONSTRAINT chk_alerta_origem
    CHECK (origem_alerta IN ('AUTOMATICO', 'MANUAL'));

ALTER TABLE leituras_telemetria
    ADD CONSTRAINT chk_telemetria_tipo_registro
    CHECK (tipo_registro IN ('OBSERVADO', 'PREVISAO')),
    ADD CONSTRAINT chk_telemetria_limites_clima
    CHECK (
        (precipitacao IS NULL OR precipitacao >= 0) AND
        (vento_velocidade IS NULL OR vento_velocidade >= 0) AND
        (vento_direcao IS NULL OR (vento_direcao >= 0 AND vento_direcao <= 360))
    );

ALTER TABLE envios_alerta_whatsapp
    ADD CONSTRAINT chk_envios_status
    CHECK (status_envio IN ('PENDENTE', 'PROCESSANDO', 'ENVIADO', 'FALHA'));

