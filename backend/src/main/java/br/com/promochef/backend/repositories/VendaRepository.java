package br.com.promochef.backend.repositories;

import br.com.promochef.backend.dto.FaturamentoDiarioDto;
import br.com.promochef.backend.models.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    //Extrai receitas agrupadas por data a partir de uma data inicial
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
}
