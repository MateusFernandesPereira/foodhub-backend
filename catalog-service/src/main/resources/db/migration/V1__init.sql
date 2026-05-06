-- V1__init.sql
-- Criação da tabela de produtos

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    available BOOLEAN NOT NULL DEFAULT true,
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para melhorar performance de queries comuns
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_available ON products(available);
CREATE INDEX idx_products_name ON products(name);

-- Comentários para documentação
COMMENT ON TABLE products IS 'Catálogo de produtos disponíveis para pedidos';
COMMENT ON COLUMN products.price IS 'Preço em reais (BRL)';
COMMENT ON COLUMN products.stock_quantity IS 'Quantidade disponível em estoque';