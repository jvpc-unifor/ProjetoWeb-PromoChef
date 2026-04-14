-- ==========================================
-- PROMOCHEF - QUERIES PARA O DASHBOARD (F03)
-- ==========================================

-- 1. Faturamento Diário (Exemplo: Últimos 7 dias)
-- Obtém o total faturado e a quantidade de vendas agrupadas por dia.
-- Será usado no gráfico de linha de faturamento.
SELECT 
    data_venda, 
    SUM(valor_total) AS faturamento, 
    COUNT(id) AS total_pedidos
FROM tb_venda
WHERE data_venda >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY data_venda
ORDER BY data_venda ASC;

-- 2. Ranking de Produtos Mais Vendidos (Top 5)
-- Conta a quantidade total de itens vendidos por produto, multiplicando pelo preço vendido.
-- Será usado no gráfico de barras horizontal (Top 5).
SELECT 
    p.id, 
    p.nome, 
    SUM(iv.quantidade) AS total_vendido, 
    SUM(iv.quantidade * iv.preco_unitario) AS receita_total
FROM tb_item_venda iv
JOIN tb_produto p ON iv.produto_id = p.id
JOIN tb_venda v ON iv.venda_id = v.id
-- Filtro de período: WHERE v.data_venda >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY p.id, p.nome
ORDER BY total_vendido DESC
LIMIT 5;

-- 3. Tabela de Produtos com Baixo Giro
-- Produtos ativos que tiveram pouquíssimas ou nenhuma venda recente.
-- Usa o LEFT JOIN para considerar produtos com 0 vendas no período analisado (ex: últimos 15 dias)
SELECT 
    p.id, 
    p.nome, 
    COALESCE(SUM(iv.quantidade), 0) AS total_vendido
FROM tb_produto p
LEFT JOIN tb_item_venda iv ON p.id = iv.produto_id
LEFT JOIN tb_venda v ON iv.venda_id = v.id AND v.data_venda >= DATE_SUB(CURDATE(), INTERVAL 15 DAY)
WHERE p.ativo = TRUE
GROUP BY p.id, p.nome
ORDER BY total_vendido ASC
LIMIT 10;
