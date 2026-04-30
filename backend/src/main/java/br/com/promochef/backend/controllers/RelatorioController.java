package br.com.promochef.backend.controllers;

import br.com.promochef.backend.repositories.LoteRepository;
import br.com.promochef.backend.services.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/desperdicio")
    public ResponseEntity<List<LoteRepository.DesperdicioIngredienteDto>> getDesperdicioMesAtual() {
        return ResponseEntity.ok(relatorioService.obterDesperdicioMesAtual());
    }

    @GetMapping("/historico")
    public ResponseEntity<List<LoteRepository.DesperdicioHistoricoDto>> getHistoricoDesperdicio() {
        return ResponseEntity.ok(relatorioService.obterHistoricoDesperdicio());
    }
}
