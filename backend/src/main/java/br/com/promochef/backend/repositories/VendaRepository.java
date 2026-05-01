package br.com.promochef.backend.repositories;

import br.com.promochef.backend.dto.FaturamentoDiarioDto;
import br.com.promochef.backend.models.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    // Extrai receitas agrupadas por data a partir de uma data inicial
    @Query(value = """
            SELECT 
                data_venda AS dataVenda, 
                SUM(valor_total) AS faturamento, 
                COUNT(id) AS totalPedidos
            FROM tb_venda
            WHERE data_venda >= :dataInicio
            GROUP BY data_venda
            ORDER BY data_venda ASC
            """, nativeQuery = true)
    List<FaturamentoDiarioDto> findFaturamentoDiarioByPeriodo(@Param("dataInicio") LocalDate dataInicio);

    // DTO para a projeção da query nativa de previsão
    interface PrevisaoDemandaDto {
        Long getProdutoId();
        String getProdutoNome();
        Integer getDiaSemana();
        Integer getTotalVendidoPeriodo();
        Double getMediaVendasEsperada();
    }

    // F07: Média de vendas por dia da semana (últimos 28 dias fechados)
    @Query(value = """
            SELECT 
                p.id AS produtoId,
                p.nome AS produtoNome,
                DAYOFWEEK(v.data_venda) AS diaSemana,
                SUM(iv.quantidade) AS totalVendidoPeriodo,
                (SUM(iv.quantidade) / 4.0) AS mediaVendasEsperada
            FROM tb_venda v
            JOIN tb_item_venda iv ON v.id = iv.venda_id
            JOIN tb_produto p ON iv.produto_id = p.id
            WHERE v.data_venda BETWEEN DATE_SUB(CURDATE(), INTERVAL 28 DAY) AND DATE_SUB(CURDATE(), INTERVAL 1 DAY)
            GROUP BY p.id, p.nome, DAYOFWEEK(v.data_venda)
            ORDER BY p.nome ASC, diaSemana ASC
            """, nativeQuery = true)
    List<PrevisaoDemandaDto> findPrevisaoDemandaUltimos28Dias();
}
