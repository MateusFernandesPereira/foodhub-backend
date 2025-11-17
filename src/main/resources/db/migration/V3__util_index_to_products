-- V3__optimize_products_indexes.sql

-- Índice composto para query comum: produtos ativos por categoria
CREATE INDEX idx_products_category_available_active
ON products(category, available)
WHERE deleted_at IS NULL;

-- Índice para busca por faixa de preço
CREATE INDEX idx_products_price ON products(price);

-- Índice para ordenação por data de criação (produtos recentes)
CREATE INDEX idx_products_created_at ON products(created_at DESC);

-- Índice parcial: apenas produtos em estoque
CREATE INDEX idx_products_in_stock
ON products(stock_quantity)
WHERE stock_quantity > 0 AND deleted_at IS NULL;