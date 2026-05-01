package br.com.promochef.backend.controllers;

import br.com.promochef.backend.repositories.ProdutoRepository.RentabilidadeDto;
import br.com.promochef.backend.services.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    // F08: Retorna a rentabilidade e margem de lucro de cada produto
    @GetMapping("/rentabilidade")
    public ResponseEntity<List<RentabilidadeDto>> getRentabilidade() {
        return ResponseEntity.ok(produtoService.obterRentabilidadeProdutos());
    }
}
