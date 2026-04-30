package br.com.promochef.backend.repositories;

import br.com.promochef.backend.models.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    // Busca lotes com validade entre hoje e a data fornecida
    @Query("SELECT l FROM Lote l WHERE l.dataValidade BETWEEN :hoje AND :dataLimite")
    List<Lote> findLotesVencendoAte(@Param("hoje") LocalDate hoje, @Param("dataLimite") LocalDate dataLimite);

    // DTOs em formato de interface para projeção JPA nativa
    interface DesperdicioIngredienteDto {
        String getIngrediente();
        Double getQuantidadePerdida();
        String getUnidade();
        Double getValorPerdidoRs();
    }

    interface DesperdicioHistoricoDto {
        String getMesAno();
        Double getValorPerdidoRs();
    }

    // 1. Total financeiro de desperdício por ingrediente (Mês Atual)
    @Query(value = """
            SELECT 
                i.nome AS ingrediente,
                SUM(l.quantidade) AS quantidadePerdida,
                i.unidade AS unidade,
                SUM(l.quantidade * l.custo_unitario) AS valorPerdidoRs
            FROM tb_lote l
            JOIN tb_ingrediente i ON l.ingrediente_id = i.id
            WHERE l.data_validade < CURDATE()
              AND MONTH(l.data_validade) = MONTH(CURDATE())
              AND YEAR(l.data_validade) = YEAR(CURDATE())
            GROUP BY i.id, i.nome, i.unidade
            ORDER BY valorPerdidoRs DESC
            """, nativeQuery = true)
    List<DesperdicioIngredienteDto> findDesperdicioPorIngredienteMesAtual();

    // 2. Histórico financeiro de desperdício (Últimos 6 Meses)
    @Query(value = """
            SELECT 
                DATE_FORMAT(l.data_validade, '%Y-%m') AS mesAno,
                SUM(l.quantidade * l.custo_unitario) AS valorPerdidoRs
            FROM tb_lote l
            WHERE l.data_validade < CURDATE()
              AND l.data_validade >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
            GROUP BY DATE_FORMAT(l.data_validade, '%Y-%m')
            ORDER BY mesAno ASC
            """, nativeQuery = true)
    List<DesperdicioHistoricoDto> findHistoricoDesperdicioUltimos6Meses();
}
