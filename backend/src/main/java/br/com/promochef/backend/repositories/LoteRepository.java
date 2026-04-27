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
}
