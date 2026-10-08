ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_email_key;
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_email_ativo ON usuario(email) WHERE deleted_at IS NULL;
