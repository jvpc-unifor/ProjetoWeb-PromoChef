-- ============================================================================
-- MOTOR DE DESPERDÍCIO - Queries Base
-- ============================================================================

-- 1. Total financeiro de desperdício por ingrediente (Mês Atual)
-- Lotes vencidos cujo mês/ano de vencimento seja o atual.
SELECT 
    i.nome AS ingrediente,
    SUM(l.quantidade) AS quantidade_perdida,
    i.unidade,
    SUM(l.quantidade * l.custo_unitario) AS valor_perdido_rs
FROM tb_lote l
JOIN tb_ingrediente i ON l.ingrediente_id = i.id
WHERE l.data_validade < CURDATE()
  AND MONTH(l.data_validade) = MONTH(CURDATE())
  AND YEAR(l.data_validade) = YEAR(CURDATE())
GROUP BY i.id, i.nome, i.unidade
ORDER BY valor_perdido_rs DESC;


-- 2. Histórico financeiro de desperdício (Últimos 6 Meses)
-- Agrupa os lotes vencidos pelo mês de validade
SELECT 
    DATE_FORMAT(l.data_validade, '%Y-%m') AS mes_ano,
    SUM(l.quantidade * l.custo_unitario) AS valor_perdido_rs
FROM tb_lote l
WHERE l.data_validade < CURDATE()
  AND l.data_validade >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
GROUP BY DATE_FORMAT(l.data_validade, '%Y-%m')
ORDER BY mes_ano ASC;
