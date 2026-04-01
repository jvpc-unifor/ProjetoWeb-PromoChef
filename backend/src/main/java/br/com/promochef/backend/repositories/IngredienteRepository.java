package br.com.promochef.backend.repositories;

import br.com.promochef.backend.models.Ingrediente;
import br.com.promochef.backend.models.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    Optional<Ingrediente> findByNome(String nome);

    default Lote saveLote(Lote lote) {
        return lote;
    }
}