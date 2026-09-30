UPDATE estabelecimento SET bairro = UPPER(bairro) WHERE bairro IS NOT NULL;
ALTER TABLE estabelecimento ALTER COLUMN bairro DROP DEFAULT;
ALTER TABLE ocorrencias ALTER COLUMN titulo DROP NOT NULL;
