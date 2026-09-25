CREATE TABLE estabelecimento_horarios(
    id BIGSERIAL PRIMARY KEY,
    dia_semana INT NOT NULL,
    horario_abertura TIME NOT NULL,
    horario_fechamento TIME NOT NULL,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento(id) ON DELETE CASCADE,
    CONSTRAINT chk_dia_semana CHECK(dia_semana BETWEEN 1 AND 7)
);

CREATE INDEX idx_horario_estabelecimento ON estabelecimento_horarios(estabelecimento_id);