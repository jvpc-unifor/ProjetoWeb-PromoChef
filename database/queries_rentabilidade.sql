-- ============================================================================
-- FUNCIONALIDADE 08: Motor de Rentabilidade e Margem de Lucro
-- ============================================================================

USE promochef_db;

-- 1. Query Principal: Ranking de rentabilidade dos produtos
-- Passo A: Descobre o custo médio de cada ingrediente com base no estoque
-- Passo B: Multiplica pela Ficha Técnica para saber o custo de produção do prato
-- Passo C: Compara com o preço de venda e calcula a Margem de Lucro %

WITH CustoIngrediente AS (
    SELECT 
        ingrediente_id,
        AVG(custo_unitario) as custo_medio
    FROM tb_lote
    GROUP BY ingrediente_id
),
CustoProduto AS (
    SELECT 
        ft.produto_id,
        SUM(ft.quantidade_usada * ci.custo_medio) as custo_producao
    FROM tb_ficha_tecnica ft
    JOIN CustoIngrediente ci ON ft.ingrediente_id = ci.ingrediente_id
    GROUP BY ft.produto_id
)
SELECT 
    p.id AS produto_id,
    p.nome AS produto_nome,
    p.preco AS preco_venda,
    COALESCE(cp.custo_producao, 0) AS custo_producao,
    (p.preco - COALESCE(cp.custo_producao, 0)) AS lucro_bruto,
    CASE 
        WHEN p.preco > 0 THEN ROUND(((p.preco - COALESCE(cp.custo_producao, 0)) / p.preco) * 100, 2)
        ELSE 0 
    END AS margem_lucro_pct
FROM tb_produto p
LEFT JOIN CustoProduto cp ON p.id = cp.produto_id
WHERE p.ativo = TRUE
ORDER BY margem_lucro_pct DESC;
