DROP INDEX IF EXISTS idx_usuario_email_ativo;
CREATE UNIQUE INDEX idx_usuario_email_ativo ON usuario(LOWER(email)) WHERE deleted_at IS NULL;

