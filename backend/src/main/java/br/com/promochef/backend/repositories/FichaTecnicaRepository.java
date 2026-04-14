package br.com.promochef.backend.repositories;

import br.com.promochef.backend.models.FichaTecnica;
import br.com.promochef.backend.models.FichaTecnicaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FichaTecnicaRepository extends JpaRepository<FichaTecnica, FichaTecnicaId> {
}
