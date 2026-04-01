CREATE DATABASE IF NOT EXISTS promochef_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE promochef_db;

-- Tabela do usuário;
CREATE TABLE IF NOT EXISTS tb_usuario
(
    id         BIGINT                    NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(100)              NOT NULL,
    email      VARCHAR(150)              NOT NULL,
    senha_hash VARCHAR(255)              NOT NULL,
    tipo       ENUM ('ADMIN', 'GERENTE') NOT NULL,
    ativo      BOOLEAN                   NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

-- Criado 2 usuários bases de test:
-- Senhas admin123 e gerente123 pelo https://bcrypt-generator.com/ com base 12;
INSERT INTO tb_usuario (nome, email, senha_hash, tipo, ativo)
VALUES ('Administrador',
        'admin@promochef.com',
        '$2a$12$CM.GgCRTnE/R8bdrBTUyLez0dOztEHzbsOACYBvVeQwxcG0Scvawu',
        'ADMIN',
        TRUE),
       ('Gerente test',
        'gerente@promochef.com',
        '$2a$12$tHAF6hTUBZTwhobYs7.RQ.SkmmR327d/0l8t1WHLp/cpecKZb9sea',
        'GERENTE',
        TRUE);

SELECT *
FROM tb_usuario;
-- DELETE FROM tb_usuario;

-- -- -- -- --

-- Tabelas dos produtos
CREATE TABLE IF NOT EXISTS tb_produto
(
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(150)  NOT NULL,
    descricao     TEXT,
    preco         DECIMAL(8, 2) NOT NULL,
    categoria     VARCHAR(80)   NOT NULL,
    ativo         BOOLEAN       NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_produto PRIMARY KEY (id),
    INDEX idx_produto_categoria (categoria),
    INDEX idx_produto_ativo (ativo)
);

-- Tabelas dos ingredientes
CREATE TABLE IF NOT EXISTS tb_ingrediente
(
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(100)  NOT NULL,
    unidade        VARCHAR(20)   NOT NULL,
    estoque_minimo DECIMAL(8, 3) NOT NULL DEFAULT 0,
    data_cadastro  TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ingrediente PRIMARY KEY (id),
    INDEX idx_ingrediente_nome (nome),
    INDEX idx_ingrediente_unidade (unidade)
);

-- Tabelas dos lotes
CREATE TABLE IF NOT EXISTS tb_lote
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    ingrediente_id BIGINT         NOT NULL,
    quantidade     DECIMAL(10, 3) NOT NULL,
    custo_unitario DECIMAL(8, 4)  NOT NULL,
    data_validade  DATE           NOT NULL,
    data_entrada   DATE           NOT NULL,
    numero_lote    VARCHAR(50),
    observacao     TEXT,

    CONSTRAINT pk_lote PRIMARY KEY (id),
    CONSTRAINT fk_lote_ingrediente FOREIGN KEY (ingrediente_id)
        REFERENCES tb_ingrediente (id) ON DELETE RESTRICT,
    INDEX idx_lote_validade (data_validade),
    INDEX idx_lote_entrada (data_entrada),
    INDEX idx_lote_ingrediente (ingrediente_id)
);

-- Tabelas fichas técnicas

CREATE TABLE IF NOT EXISTS tb_ficha_tecnica
(
    produto_id       BIGINT         NOT NULL,
    ingrediente_id   BIGINT         NOT NULL,
    quantidade_usada DECIMAL(10, 4) NOT NULL,
    unidade          VARCHAR(20)    NOT NULL,

    CONSTRAINT pk_ficha_tecnica PRIMARY KEY (produto_id, ingrediente_id),
    CONSTRAINT fk_ficha_produto FOREIGN KEY (produto_id)
        REFERENCES tb_produto (id) ON DELETE CASCADE,
    CONSTRAINT fk_ficha_ingrediente FOREIGN KEY (ingrediente_id)
        REFERENCES tb_ingrediente (id) ON DELETE RESTRICT,
    INDEX idx_ficha_produto (produto_id),
    INDEX idx_ficha_ingrediente (ingrediente_id)
);

-- Tabelas de vendas (o cabeçalho)
CREATE TABLE IF NOT EXISTS tb_venda
(
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    usuario_id  BIGINT,
    data_venda  DATE           NOT NULL,
    valor_total DECIMAL(10, 2) NOT NULL,
    origem      VARCHAR(20)    NOT NULL DEFAULT 'importado',

    CONSTRAINT pk_venda PRIMARY KEY (id),
    CONSTRAINT fk_venda_usuario FOREIGN KEY (usuario_id)
        REFERENCES tb_usuario (id) ON DELETE SET NULL,
    INDEX idx_venda_data (data_venda),
    INDEX idx_venda_origem (origem),
    INDEX idx_venda_usuario (usuario_id)
);

-- Tabela dos intes de vendas
CREATE TABLE IF NOT EXISTS tb_item_venda
(
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    venda_id       BIGINT        NOT NULL,
    produto_id     BIGINT        NOT NULL,
    quantidade     INTEGER       NOT NULL,
    preco_unitario DECIMAL(8, 2) NOT NULL,

    CONSTRAINT pk_item_venda PRIMARY KEY (id),
    CONSTRAINT fk_item_venda FOREIGN KEY (venda_id)
        REFERENCES tb_venda (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id)
        REFERENCES tb_produto (id) ON DELETE RESTRICT,
    INDEX idx_item_venda (venda_id),
    INDEX idx_item_produto (produto_id)
);

-- Tabelas de Promoções (Motor de Sugestões)
CREATE TABLE IF NOT EXISTS tb_promocao
(
    id            BIGINT                                 NOT NULL AUTO_INCREMENT,
    produto_id    BIGINT                                 NOT NULL,
    desconto_pct  INTEGER                                NOT NULL,
    motivo        TEXT                                   NOT NULL,
    status        ENUM ('SUGESTAO', 'ATIVA', 'RECUSADA') NOT NULL,
    data_sugestao DATE                                   NOT NULL,
    data_ativacao DATE,

    CONSTRAINT pk_promocao PRIMARY KEY (id),
    CONSTRAINT fk_promocao_produto FOREIGN KEY (produto_id)
        REFERENCES tb_produto (id) ON DELETE CASCADE,
    INDEX idx_promocao_status (status),
    INDEX idx_promocao_data (data_sugestao),
    INDEX idx_promocao_produto (produto_id)
);

-- tabela de Alertas (Validade e Estoque)
CREATE TABLE IF NOT EXISTS tb_alerta
(
    id          BIGINT                                NOT NULL AUTO_INCREMENT,
    lote_id     BIGINT                                NOT NULL,
    tipo        ENUM ('VENCIMENTO', 'ESTOQUE_MINIMO') NOT NULL,
    mensagem    TEXT                                  NOT NULL,
    data_alerta DATE                                  NOT NULL,
    visualizado BOOLEAN                               NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_alerta PRIMARY KEY (id),
    CONSTRAINT fk_alerta_lote FOREIGN KEY (lote_id)
        REFERENCES tb_lote (id) ON DELETE CASCADE,
    INDEX idx_alerta_tipo (tipo),
    INDEX idx_alerta_visualizado (visualizado),
    INDEX idx_alerta_data (data_alerta)
);

-- SEEDs: Criado por ai para fazer testes:
-- ----------------------------------------------------------------------------
-- 2. Produtos (5 produtos do cardápio)
-- ----------------------------------------------------------------------------
INSERT INTO tb_produto (nome, descricao, preco, categoria, ativo)
VALUES ('X-Burguer Clássico', 'Hambúrguer artesanal com queijo, alface e tomate', 28.90, 'Lanches', TRUE),
       ('Pizza Margherita', 'Pizza tradicional com molho, mozzarella e manjericão', 45.00, 'Pizzas', TRUE),
       ('Caesar Salad', 'Salada com alface romana, croutons e molho caesar', 22.50, 'Saladas', TRUE),
       ('Suco de Laranja', 'Suco natural da fruta 500ml', 12.00, 'Bebidas', TRUE),
       ('Brownie com Sorvete', 'Brownie de chocolate com sorvete de creme', 18.00, 'Sobremesas', TRUE);

-- ----------------------------------------------------------------------------
-- 3. Ingredientes (8 ingredientes principais)
-- ----------------------------------------------------------------------------
INSERT INTO tb_ingrediente (nome, unidade, estoque_minimo)
VALUES ('Pão de Hambúrguer', 'un', 50.000),
       ('Carne Bovina', 'kg', 5.000),
       ('Queijo Mussarela', 'kg', 3.000),
       ('Tomate', 'kg', 4.000),
       ('Alface', 'kg', 2.000),
       ('Farinha de Trigo', 'kg', 10.000),
       ('Molho de Tomate', 'L', 5.000),
       ('Laranja', 'kg', 8.000);

-- ----------------------------------------------------------------------------
-- 4. Lotes (10 lotes com validade variada para testar alertas)
-- Alguns vencendo em breve para gerar alertas na Sprint 04
-- ----------------------------------------------------------------------------
INSERT INTO tb_lote (ingrediente_id, quantidade, custo_unitario, data_validade, data_entrada, numero_lote, observacao)
VALUES
-- Pão de Hambúrguer (ingrediente_id = 1)
(1, 100.000, 0.8500, DATE_ADD(CURDATE(), INTERVAL 5 DAY), CURDATE(), 'LOT-2026-001', 'Fornecedor: Padaria Central'),
(1, 150.000, 0.8200, DATE_ADD(CURDATE(), INTERVAL 15 DAY), CURDATE(), 'LOT-2026-002', NULL),

-- Carne Bovina (ingrediente_id = 2)
(2, 20.000, 35.0000, DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'LOT-2026-003',
 '⚠️ Vence em breve!'),
(2, 25.000, 34.5000, DATE_ADD(CURDATE(), INTERVAL 10 DAY), CURDATE(), 'LOT-2026-004', NULL),

-- Queijo Mussarela (ingrediente_id = 3)
(3, 15.000, 42.0000, DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'LOT-2026-005',
 '⚠️ Vence amanhã!'),
(3, 20.000, 41.5000, DATE_ADD(CURDATE(), INTERVAL 8 DAY), CURDATE(), 'LOT-2026-006', NULL),

-- Tomate (ingrediente_id = 4)
(4, 30.000, 8.5000, DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'LOT-2026-007',
 '⚠️ Vence em 3 dias'),
(4, 25.000, 8.0000, DATE_ADD(CURDATE(), INTERVAL 12 DAY), CURDATE(), 'LOT-2026-008', NULL),

-- Laranja (ingrediente_id = 8)
(8, 50.000, 4.5000, DATE_ADD(CURDATE(), INTERVAL 7 DAY), CURDATE(), 'LOT-2026-009', 'Safra recente'),
(8, 40.000, 4.2000, DATE_ADD(CURDATE(), INTERVAL 20 DAY), CURDATE(), 'LOT-2026-010', NULL);

-- ----------------------------------------------------------------------------
-- 5. Ficha Técnica (Receitas dos 5 produtos)
-- ----------------------------------------------------------------------------
INSERT INTO tb_ficha_tecnica (produto_id, ingrediente_id, quantidade_usada, unidade)
VALUES
-- X-Burguer Clássico (produto_id = 1)
(1, 1, 1.0000, 'un'), -- 1 pão
(1, 2, 0.1800, 'kg'), -- 180g carne
(1, 3, 0.0500, 'kg'), -- 50g queijo
(1, 4, 0.0800, 'kg'), -- 80g tomate
(1, 5, 0.0300, 'kg'), -- 30g alface

-- Pizza Margherita (produto_id = 2)
(2, 6, 0.3000, 'kg'), -- 300g farinha
(2, 7, 0.1500, 'L'),  -- 150ml molho
(2, 3, 0.2000, 'kg'), -- 200g queijo
(2, 4, 0.1000, 'kg'), -- 100g tomate

-- Caesar Salad (produto_id = 3)
(3, 5, 0.1500, 'kg'), -- 150g alface
(3, 4, 0.0500, 'kg'), -- 50g tomate

-- Suco de Laranja (produto_id = 4)
(4, 8, 0.4000, 'kg'), -- 400g laranja

-- Brownie com Sorvete (produto_id = 5)
(5, 6, 0.1000, 'kg');
-- 100g farinha (simplificado)

-- ----------------------------------------------------------------------------
-- 6. Vendas (30 vendas dos últimos 15 dias para dashboard)
-- ----------------------------------------------------------------------------
INSERT INTO tb_venda (usuario_id, data_venda, valor_total, origem)
VALUES
-- Semana 1 (últimos 15 dias)
(NULL, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 156.80, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 14 DAY), 89.50, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 234.00, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 13 DAY), 67.40, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 312.50, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 145.00, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 198.70, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 11 DAY), 76.00, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 267.30, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 123.50, 'importado'),

-- Semana NULL
(NULL, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 189.00, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 9 DAY), 95.40, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 278.60, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 134.20, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 345.00, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 167.80, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 212.40, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 98.50, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 289.70, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 156.30, 'importado'),

-- Semana 3 (mais recente)
(NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 324.50, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 178.00, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 267.80, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 145.60, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 398.00, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 189.40, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 412.50, 'importado'),
(NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 234.00, 'importado'),
(NULL, CURDATE(), 356.70, 'importado'),
(NULL, CURDATE(), 198.50, 'importado');

-- ----------------------------------------------------------------------------
-- 7. Itens das Vendas (Distribuição de produtos nas 30 vendas)
-- Simula X-Burguer como mais vendido, Suco como segundo
-- ----------------------------------------------------------------------------
INSERT INTO tb_item_venda (venda_id, produto_id, quantidade, preco_unitario)
VALUES
-- Vendas 1-10 (X-Burguer predominante)
(1, 1, 3, 28.90),
(1, 4, 2, 12.00),
(2, 1, 2, 28.90),
(2, 3, 1, 22.50),
(3, 1, 5, 28.90),
(3, 2, 1, 45.00),
(3, 4, 3, 12.00),
(4, 1, 1, 28.90),
(4, 4, 2, 12.00),
(4, 5, 1, 18.00),
(5, 1, 6, 28.90),
(5, 2, 2, 45.00),
(5, 4, 4, 12.00),
(6, 1, 3, 28.90),
(6, 3, 2, 22.50),
(7, 1, 4, 28.90),
(7, 4, 3, 12.00),
(7, 5, 1, 18.00),
(8, 1, 2, 28.90),
(8, 4, 1, 12.00),
(9, 1, 5, 28.90),
(9, 2, 1, 45.00),
(9, 4, 2, 12.00),
(10, 1, 3, 28.90),
(10, 3, 1, 22.50),
(10, 5, 1, 18.00),

-- Vendas 11-20
(11, 1, 4, 28.90),
(11, 4, 2, 12.00),
(12, 1, 2, 28.90),
(12, 4, 3, 12.00),
(12, 5, 1, 18.00),
(13, 1, 5, 28.90),
(13, 2, 1, 45.00),
(13, 4, 2, 12.00),
(14, 1, 3, 28.90),
(14, 3, 2, 22.50),
(15, 1, 7, 28.90),
(15, 2, 1, 45.00),
(15, 4, 3, 12.00),
(16, 1, 4, 28.90),
(16, 4, 2, 12.00),
(16, 5, 1, 18.00),
(17, 1, 3, 28.90),
(17, 3, 1, 22.50),
(17, 4, 2, 12.00),
(18, 1, 2, 28.90),
(18, 4, 2, 12.00),
(19, 1, 5, 28.90),
(19, 2, 1, 45.00),
(19, 4, 3, 12.00),
(20, 1, 4, 28.90),
(20, 3, 1, 22.50),
(20, 5, 1, 18.00),

-- Vendas 21-30 (mais recentes)
(21, 1, 6, 28.90),
(21, 2, 1, 45.00),
(21, 4, 3, 12.00),
(22, 1, 4, 28.90),
(22, 4, 2, 12.00),
(22, 5, 1, 18.00),
(23, 1, 5, 28.90),
(23, 3, 2, 22.50),
(23, 4, 2, 12.00),
(24, 1, 3, 28.90),
(24, 4, 3, 12.00),
(25, 1, 8, 28.90),
(25, 2, 2, 45.00),
(25, 4, 4, 12.00),
(26, 1, 5, 28.90),
(26, 3, 1, 22.50),
(26, 5, 1, 18.00),
(27, 1, 7, 28.90),
(27, 2, 1, 45.00),
(27, 4, 3, 12.00),
(28, 1, 4, 28.90),
(28, 4, 4, 12.00),
(28, 5, 2, 18.00),
(29, 1, 6, 28.90),
(29, 2, 1, 45.00),
(29, 3, 1, 22.50),
(29, 4, 2, 12.00),
(30, 1, 4, 28.90),
(30, 4, 3, 12.00),
(30, 5, 1, 18.00);

-- ============================================================================
-- VALIDAÇÃO: Queries para verificar os dados inseridos
-- ============================================================================

SELECT 'tb_usuario' AS tabela, COUNT(*) AS registros
FROM tb_usuario
UNION ALL
SELECT 'tb_produto', COUNT(*)
FROM tb_produto
UNION ALL
SELECT 'tb_ingrediente', COUNT(*)
FROM tb_ingrediente
UNION ALL
SELECT 'tb_lote', COUNT(*)
FROM tb_lote
UNION ALL
SELECT 'tb_ficha_tecnica', COUNT(*)
FROM tb_ficha_tecnica
UNION ALL
SELECT 'tb_venda', COUNT(*)
FROM tb_venda
UNION ALL
SELECT 'tb_item_venda', COUNT(*)
FROM tb_item_venda
UNION ALL
SELECT 'tb_promocao', COUNT(*)
FROM tb_promocao
UNION ALL
SELECT 'tb_alerta', COUNT(*)
FROM tb_alerta;

-- Verificar lotes próximos ao vencimento (para testes de alerta)
SELECT l.id,
       i.nome                               AS ingrediente,
       l.quantidade,
       l.data_validade,
       DATEDIFF(l.data_validade, CURDATE()) AS dias_para_vencer
FROM tb_lote l
         JOIN tb_ingrediente i ON l.ingrediente_id = i.id
WHERE l.data_validade <= DATE_ADD(CURDATE(), INTERVAL 5 DAY)
ORDER BY l.data_validade ASC;

--
-- DROP database promochef_db;