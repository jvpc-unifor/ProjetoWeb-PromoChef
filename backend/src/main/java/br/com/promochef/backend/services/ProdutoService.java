package br.com.promochef.backend.services;

import br.com.promochef.backend.repositories.ProdutoRepository;
import br.com.promochef.backend.repositories.ProdutoRepository.RentabilidadeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    /**
     * F08: Retorna o ranking de rentabilidade calculando a margem de lucro
     * com base no custo atual dos ingredientes e o preço de venda.
     */
    public List<RentabilidadeDto> obterRentabilidadeProdutos() {
        return produtoRepository.findRentabilidadeProdutos();
    }
}
