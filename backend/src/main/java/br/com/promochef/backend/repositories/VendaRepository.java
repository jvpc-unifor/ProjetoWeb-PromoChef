package br.com.promochef.backend.repositories;

import br.com.promochef.backend.models.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository<Venda, Long> {
}
