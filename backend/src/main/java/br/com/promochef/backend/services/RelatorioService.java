package br.com.promochef.backend.services;

import br.com.promochef.backend.repositories.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final LoteRepository loteRepository;

    public List<LoteRepository.DesperdicioIngredienteDto> obterDesperdicioMesAtual() {
        return loteRepository.findDesperdicioPorIngredienteMesAtual();
    }

    public List<LoteRepository.DesperdicioHistoricoDto> obterHistoricoDesperdicio() {
        return loteRepository.findHistoricoDesperdicioUltimos6Meses();
    }
}
