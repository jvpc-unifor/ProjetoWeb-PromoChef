-- Query para detectar lotes vencendo em até 3 dias
SELECT 
    l.id AS lote_id,
    i.nome AS ingrediente,
    l.data_validade,
    DATEDIFF(l.data_validade, CURDATE()) AS dias_para_vencer,
    l.quantidade
FROM tb_lote l
JOIN tb_ingrediente i ON l.ingrediente_id = i.id
WHERE l.data_validade BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 3 DAY)
ORDER BY l.data_validade ASC;

-- Query para inserir o alerta na tb_alerta caso não exista alerta pendente para o lote
INSERT INTO tb_alerta (lote_id, tipo, mensagem, data_alerta, visualizado)
SELECT 
    l.id, 
    'VENCIMENTO', 
    CONCAT('Atenção: O lote do ingrediente ', i.nome, ' vence em ', DATEDIFF(l.data_validade, CURDATE()), ' dia(s).'), 
    CURDATE(), 
    FALSE
FROM tb_lote l
JOIN tb_ingrediente i ON l.ingrediente_id = i.id
WHERE l.data_validade BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 3 DAY)
  AND NOT EXISTS (
      SELECT 1 FROM tb_alerta a 
      WHERE a.lote_id = l.id 
        AND a.tipo = 'VENCIMENTO' 
        AND a.visualizado = FALSE
  );
