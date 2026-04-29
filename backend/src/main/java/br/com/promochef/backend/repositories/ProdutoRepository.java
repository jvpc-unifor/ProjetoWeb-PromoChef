package br.com.promochef.backend.repositories;

import br.com.promochef.backend.dto.ProdutoRankingDto;
import br.com.promochef.backend.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    java.util.Optional<Produto> findByNome(String nome);

    // Retorna os Top 5 produtos mais vendidos globalmente
    @Query(value = """
            SELECT 
                p.id AS id, 
                p.nome AS nome, 
                SUM(iv.quantidade) AS totalVendido, 
                SUM(iv.quantidade * iv.preco_unitario) AS receitaTotal
            FROM tb_item_venda iv
            JOIN tb_produto p ON iv.produto_id = p.id
            JOIN tb_venda v ON iv.venda_id = v.id
            WHERE p.ativo = TRUE
            GROUP BY p.id, p.nome
            ORDER BY totalVendido DESC
            LIMIT 5
            """, nativeQuery = true)
    List<ProdutoRankingDto> findTop5MaisVendidos();

    // Retorna os 10 produtos que menos venderam desde a data selecionada
    @Query(value = """
            SELECT 
                p.id AS id, 
                p.nome AS nome, 
                COALESCE(SUM(iv.quantidade), 0) AS totalVendido,
                COALESCE(SUM(iv.quantidade * iv.preco_unitario), 0) AS receitaTotal
            FROM tb_produto p
            LEFT JOIN tb_item_venda iv ON p.id = iv.produto_id
            LEFT JOIN tb_venda v ON iv.venda_id = v.id AND v.data_venda >= :dataLimite
            WHERE p.ativo = TRUE
            GROUP BY p.id, p.nome
            ORDER BY totalVendido ASC
            LIMIT 10
            """, nativeQuery = true)
    List<ProdutoRankingDto> findProdutosComBaixoGiro(@Param("dataLimite") LocalDate dataLimite);

    // Interface para mapear o retorno da query nativa de cruzamento de lotes
    interface ProdutoPromocaoSugestaoDto {
        Long getProdutoId();
        Integer getDiasParaVencer();
    }

    // Busca os produtos cujos ingredientes estão vencendo em até X dias
    @Query(value = """
            SELECT 
                p.id AS produtoId, 
                MIN(DATEDIFF(l.data_validade, CURDATE())) AS diasParaVencer
            FROM tb_produto p
            JOIN tb_ficha_tecnica ft ON p.id = ft.produto_id
            JOIN tb_lote l ON ft.ingrediente_id = l.ingrediente_id
            WHERE l.data_validade BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL :dias DAY)
            GROUP BY p.id
            """, nativeQuery = true)
    List<ProdutoPromocaoSugestaoDto> findProdutosComIngredientesVencendoEmAte(@Param("dias") int dias);
}
