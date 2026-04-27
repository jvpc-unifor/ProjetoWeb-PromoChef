package br.com.promochef.backend.repositories;

import br.com.promochef.backend.models.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    
    // Busca alertas não visualizados do tipo VENCIMENTO
    List<Alerta> findByTipoAndVisualizadoFalse(br.com.promochef.backend.models.TipoAlerta tipo);

    // Verifica se já existe um alerta de vencimento não visualizado para um lote
    boolean existsByLoteIdAndTipoAndVisualizadoFalse(Long loteId, br.com.promochef.backend.models.TipoAlerta tipo);
}
