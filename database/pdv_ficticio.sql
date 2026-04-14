-- Criação do banco de dados fictício do PDV
CREATE DATABASE IF NOT EXISTS pdv_ficticio;
USE pdv_ficticio;

-- Tabela de Produtos do PDV
-- Armazena o catálogo de produtos que são vendidos ou usados como ingredientes
CREATE TABLE IF NOT EXISTS pdv_produto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    preco_venda DECIMAL(10, 2) NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL
);

-- Tabela de Compras
-- Registra a entrada de mercadorias no PDV, gerando dados que servem como lotes
CREATE TABLE IF NOT EXISTS pdv_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id BIGINT NOT NULL,
    data_compra DATE NOT NULL,
    quantidade DECIMAL(10, 3) NOT NULL,
    preco_custo DECIMAL(10, 2) NOT NULL,
    data_validade DATE,
    FOREIGN KEY (produto_id) REFERENCES pdv_produto(id)
);

-- Tabela de Estoque
-- Representa a quantidade atual de cada lote de produto disponível no restaurante
CREATE TABLE IF NOT EXISTS pdv_estoque (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id BIGINT NOT NULL,
    lote_id BIGINT,
    quantidade_atual DECIMAL(10, 3) NOT NULL,
    data_validade DATE,
    FOREIGN KEY (produto_id) REFERENCES pdv_produto(id),
    FOREIGN KEY (lote_id) REFERENCES pdv_compra(id)
);

-- Tabela de Vendas
-- Armazena o histórico de vendas realizadas pelo restaurante, usado para cruzar dados posteriormente
CREATE TABLE IF NOT EXISTS pdv_venda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id BIGINT NOT NULL,
    data_venda DATETIME NOT NULL,
    quantidade DECIMAL(10, 3) NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    valor_total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (produto_id) REFERENCES pdv_produto(id)
);
