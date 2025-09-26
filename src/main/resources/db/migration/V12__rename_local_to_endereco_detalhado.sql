-- Migration para renomear coluna 'local' para 'endereco_detalhado' na tabela Patio
-- Criado para evitar confusão entre 'local' e 'localizacao'

ALTER TABLE patio RENAME COLUMN local TO endereco_detalhado;
