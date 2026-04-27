package br.com.promochef.backend.services;

import br.com.promochef.backend.models.Alerta;
import br.com.promochef.backend.models.Lote;
import br.com.promochef.backend.models.TipoAlerta;
import br.com.promochef.backend.repositories.AlertaRepository;
import br.com.promochef.backend.repositories.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final LoteRepository loteRepository;

    // Roda todo dia à meia-noite
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void verificarVencimentos() {
        log.info("Iniciando verificação diária de vencimento de lotes...");
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(3);

        List<Lote> lotesVencendo = loteRepository.findLotesVencendoAte(hoje, limite);

        int alertasCriados = 0;
        for (Lote lote : lotesVencendo) {
            boolean jaAvisado = alertaRepository.existsByLoteIdAndTipoAndVisualizadoFalse(lote.getId(), TipoAlerta.VENCIMENTO);
            
            if (!jaAvisado) {
                long diasRestantes = ChronoUnit.DAYS.between(hoje, lote.getDataValidade());
                String msg = String.format("Atenção: O lote %s do ingrediente %s vence em %d dia(s).",
                        lote.getNumeroLote() != null ? lote.getNumeroLote() : lote.getId().toString(),
                        lote.getIngrediente().getNome(),
                        diasRestantes);

                Alerta alerta = new Alerta(null, lote, TipoAlerta.VENCIMENTO, msg, hoje, false);
                alertaRepository.save(alerta);
                alertasCriados++;
            }
        }
        log.info("Verificação concluída. {} novos alertas gerados.", alertasCriados);
    }

    public List<Alerta> getAlertasNaoVisualizados(TipoAlerta tipo) {
        return alertaRepository.findByTipoAndVisualizadoFalse(tipo);
    }

    @Transactional
    public void marcarComoVisualizado(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado: " + id));
        alerta.setVisualizado(true);
        alertaRepository.save(alerta);
    }
}
