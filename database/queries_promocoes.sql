-- ============================================================================
-- MOTOR DE PROMOÇÕES - Queries Base
-- ============================================================================

-- 1. Buscar produtos impactados por ingredientes vencendo em até 5 dias
SELECT DISTINCT 
    p.id AS produto_id, 
    p.nome AS produto, 
    p.preco,
    MIN(DATEDIFF(l.data_validade, CURDATE())) AS dias_para_vencer
FROM tb_produto p
JOIN tb_ficha_tecnica ft ON p.id = ft.produto_id
JOIN tb_lote l ON ft.ingrediente_id = l.ingrediente_id
WHERE l.data_validade BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 5 DAY)
GROUP BY p.id, p.nome, p.preco
ORDER BY dias_para_vencer ASC;

-- 2. Inserir sugestão de promoção evitando duplicidades
-- Regras de desconto (Sprint 04): 
-- 1 dia = 30% | 2 dias = 20% | 3-5 dias = 10%
INSERT INTO tb_promocao (produto_id, desconto_pct, motivo, status, data_sugestao)
SELECT 
    sub.produto_id,
    CASE 
        WHEN sub.dias_para_vencer <= 1 THEN 30
        WHEN sub.dias_para_vencer = 2 THEN 20
        ELSE 10 
    END AS desconto_pct,
    CONCAT('Ingrediente estratégico vence em ', sub.dias_para_vencer, ' dia(s)') AS motivo,
    'SUGESTAO' AS status,
    CURDATE() AS data_sugestao
FROM (
    SELECT 
        p.id AS produto_id, 
        MIN(DATEDIFF(l.data_validade, CURDATE())) AS dias_para_vencer
    FROM tb_produto p
    JOIN tb_ficha_tecnica ft ON p.id = ft.produto_id
    JOIN tb_lote l ON ft.ingrediente_id = l.ingrediente_id
    WHERE l.data_validade BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 5 DAY)
    GROUP BY p.id
) sub
WHERE NOT EXISTS (
    -- Não criar nova sugestão se já houver uma ativa ou sugerida hoje para o mesmo produto
    SELECT 1 FROM tb_promocao promo 
    WHERE promo.produto_id = sub.produto_id 
      AND (promo.status = 'ATIVA' OR (promo.status = 'SUGESTAO' AND promo.data_sugestao = CURDATE()))
);
