package br.com.promochef.backend.services;

import br.com.promochef.backend.models.Produto;
import br.com.promochef.backend.models.Promocao;
import br.com.promochef.backend.models.StatusPromocao;
import br.com.promochef.backend.repositories.ProdutoRepository;
import br.com.promochef.backend.repositories.PromocaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotorPromocaoService {

    private final ProdutoRepository produtoRepository;
    private final PromocaoRepository promocaoRepository;

    // Roda todo dia à meia-noite e quinze
    @Scheduled(cron = "0 15 0 * * ?")
    @Transactional
    public void gerarSugestoesPromocao() {
        log.info("Iniciando Motor de Promoções...");
        LocalDate hoje = LocalDate.now();

        // Busca produtos afetados por lotes vencendo em até 5 dias
        var sugestoesBase = produtoRepository.findProdutosComIngredientesVencendoEmAte(5);

        int criadas = 0;
        for (var base : sugestoesBase) {
            Long produtoId = base.getProdutoId();
            int diasVencer = base.getDiasParaVencer();

            if (!promocaoRepository.existsByProdutoAndStatusValido(produtoId, hoje)) {
                Produto produto = produtoRepository.findById(produtoId).orElse(null);
                if (produto == null) continue;

                int descontoPct;
                if (diasVencer <= 1) {
                    descontoPct = 30;
                } else if (diasVencer == 2) {
                    descontoPct = 20;
                } else {
                    descontoPct = 10;
                }

                String motivo = String.format("Ingrediente estratégico vence em %d dia(s)", diasVencer);

                Promocao promo = new Promocao(null, produto, descontoPct, motivo, StatusPromocao.SUGESTAO, hoje, null);
                promocaoRepository.save(promo);
                criadas++;
            }
        }
        log.info("Motor de Promoções finalizado. {} novas sugestões criadas.", criadas);
    }

    public List<Promocao> listarSugestoes() {
        return promocaoRepository.findByStatus(StatusPromocao.SUGESTAO);
    }

    @Transactional
    public void ativarPromocao(Long id) {
        Promocao promo = promocaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoção não encontrada"));
        promo.setStatus(StatusPromocao.ATIVA);
        promo.setDataAtivacao(LocalDate.now());
        promocaoRepository.save(promo);
    }

    @Transactional
    public void recusarPromocao(Long id) {
        Promocao promo = promocaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoção não encontrada"));
        promo.setStatus(StatusPromocao.RECUSADA);
        promocaoRepository.save(promo);
    }
}
