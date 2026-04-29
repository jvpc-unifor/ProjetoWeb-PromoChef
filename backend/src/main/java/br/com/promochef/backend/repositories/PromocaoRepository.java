package br.com.promochef.backend.repositories;

import br.com.promochef.backend.models.Promocao;
import br.com.promochef.backend.models.StatusPromocao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

    // Retorna as sugestões pendentes
    List<Promocao> findByStatus(StatusPromocao status);

    // Verifica se já existe promoção sugerida ou ativa hoje para não duplicar
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Promocao p " +
           "WHERE p.produto.id = :produtoId " +
           "AND (p.status = 'ATIVA' OR (p.status = 'SUGESTAO' AND p.dataSugestao = :hoje))")
    boolean existsByProdutoAndStatusValido(@Param("produtoId") Long produtoId, @Param("hoje") LocalDate hoje);
}
