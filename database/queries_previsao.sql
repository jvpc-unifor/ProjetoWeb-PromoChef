-- ============================================================================
-- FUNCIONALIDADE 07: Motor de Previsão de Demanda (Dashboard)
-- ============================================================================

USE promochef_db;

-- 1. Query Principal: Média de vendas por dia da semana (Últimos 28 dias)
-- Pega exatas 4 semanas fechadas (ontem até 28 dias atrás).
-- Como temos 4 ocorrências de cada dia da semana nesse período, a média é a soma dividida por 4.
-- DAYOFWEEK(data): 1 = Domingo, 2 = Segunda, ..., 7 = Sábado.

SELECT 
    p.id AS produto_id,
    p.nome AS produto_nome,
    DAYOFWEEK(v.data_venda) AS dia_semana,
    SUM(iv.quantidade) AS total_vendido_periodo,
    (SUM(iv.quantidade) / 4.0) AS media_vendas_esperada
FROM tb_venda v
JOIN tb_item_venda iv ON v.id = iv.venda_id
JOIN tb_produto p ON iv.produto_id = p.id
WHERE v.data_venda BETWEEN DATE_SUB(CURDATE(), INTERVAL 28 DAY) AND DATE_SUB(CURDATE(), INTERVAL 1 DAY)
GROUP BY p.id, p.nome, DAYOFWEEK(v.data_venda)
ORDER BY p.nome ASC, dia_semana ASC;
