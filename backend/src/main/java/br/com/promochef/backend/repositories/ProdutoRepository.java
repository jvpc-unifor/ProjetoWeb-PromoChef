package br.com.promochef.backend.repositories;

import br.com.promochef.backend.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    java.util.Optional<Produto> findByNome(String nome);
}
