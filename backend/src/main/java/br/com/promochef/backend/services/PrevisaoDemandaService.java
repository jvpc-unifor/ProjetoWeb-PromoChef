package br.com.promochef.backend.services;

import br.com.promochef.backend.repositories.VendaRepository;
import br.com.promochef.backend.repositories.VendaRepository.PrevisaoDemandaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrevisaoDemandaService {

    private final VendaRepository vendaRepository;

    /**
     * F07: Retorna a previsão de demanda calculando a média de vendas 
     * por dia da semana com base nas últimas 4 semanas fechadas (28 dias).
     */
    public List<PrevisaoDemandaDto> obterPrevisaoDemanda() {
        return vendaRepository.findPrevisaoDemandaUltimos28Dias();
    }
}
