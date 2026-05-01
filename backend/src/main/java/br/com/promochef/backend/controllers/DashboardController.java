package br.com.promochef.backend.controllers;

import br.com.promochef.backend.dto.DashboardKpisDto;
import br.com.promochef.backend.dto.FaturamentoDiarioDto;
import br.com.promochef.backend.dto.ProdutoRankingDto;
import br.com.promochef.backend.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Recebe as requisições de consulta para popular os gráficos F03
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final br.com.promochef.backend.services.PrevisaoDemandaService previsaoDemandaService;

    // Injeção de dependência via construtor (recomendado pelo Spring)
    public DashboardController(DashboardService dashboardService, br.com.promochef.backend.services.PrevisaoDemandaService previsaoDemandaService) {
        this.dashboardService = dashboardService;
        this.previsaoDemandaService = previsaoDemandaService;
    }

    // Ponto de acesso para os KPIs macro (cartões do topo)
    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpisDto> getKpis() {
        DashboardKpisDto kpis = dashboardService.obterKpisDoMes();
        return ResponseEntity.ok(kpis);
    }

    // Ponto de acesso para agrupar todas as séries e tabelas dos gráficos
    @GetMapping("/vendas")
    public ResponseEntity<Map<String, Object>> getVendas() {
        Map<String, Object> response = new HashMap<>();

        // Coletando os 3 formatos de relatórios solicitados
        List<FaturamentoDiarioDto> faturamento = dashboardService.obterFaturamentoDiario();
        List<ProdutoRankingDto> top5 = dashboardService.obterTop5Produtos();
        List<ProdutoRankingDto> baixoGiro = dashboardService.obterProdutosBaixoGiro();

        // Anexando em um Map para o Frontend consumir tudo numa única chamada
        response.put("faturamentoDiario", faturamento);
        response.put("top5Produtos", top5);
        response.put("produtosBaixoGiro", baixoGiro);

        return ResponseEntity.ok(response);
    }

    // F07: Ponto de acesso para o motor de previsão de demanda
    @GetMapping("/previsao")
    public ResponseEntity<List<br.com.promochef.backend.repositories.VendaRepository.PrevisaoDemandaDto>> getPrevisaoDemanda() {
        return ResponseEntity.ok(previsaoDemandaService.obterPrevisaoDemanda());
    }
}
