-- V2__add_soft_delete_to_products.sql
-- Adiciona suporte para soft delete na tabela products

ALTER TABLE products
ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;

-- Índice para melhorar performance de queries que filtram por soft delete
CREATE INDEX idx_products_deleted_at ON products(deleted_at);

-- Comentário para documentação
COMMENT ON COLUMN products.deleted_at IS 'Data/hora da exclusão lógica (soft delete). NULL indica registro ativo';