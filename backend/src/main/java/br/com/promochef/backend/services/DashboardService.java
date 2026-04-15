package br.com.promochef.backend.services;

import br.com.promochef.backend.dto.DashboardKpisDto;
import br.com.promochef.backend.dto.FaturamentoDiarioDto;
import br.com.promochef.backend.dto.ProdutoRankingDto;
import br.com.promochef.backend.repositories.ProdutoRepository;
import br.com.promochef.backend.repositories.VendaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;

    public DashboardService(VendaRepository vendaRepository, ProdutoRepository produtoRepository) {
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
    }

    //Calcula os KPIs do último mês somando os dias
    public DashboardKpisDto obterKpisDoMes() {
        LocalDate trintaDiasAtras = LocalDate.now().minusDays(30);
        List<FaturamentoDiarioDto> vendasDoMes = vendaRepository.findFaturamentoDiarioByPeriodo(trintaDiasAtras);

        BigDecimal faturamentoTotal = BigDecimal.ZERO;
        int totalPedidos = 0;

        // Somando cada dia iterativamente
        for (FaturamentoDiarioDto dia : vendasDoMes) {
            if (dia.getFaturamento() != null) {
                faturamentoTotal = faturamentoTotal.add(dia.getFaturamento());
            }
            if (dia.getTotalPedidos() != null) {
                totalPedidos += dia.getTotalPedidos();
            }
        }

        // Obtendo o ranking para também descobrir os totais de itens vendidos (aproximação para os top 5)
        List<ProdutoRankingDto> topProdutos = produtoRepository.findTop5MaisVendidos();
        int totalProdutosVendidos = 0;
        for (ProdutoRankingDto prod : topProdutos) {
            if (prod.getTotalVendido() != null) {
                totalProdutosVendidos += prod.getTotalVendido();
            }
        }

        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (totalPedidos > 0) {
            // Dividindo com arredondamento para 2 casas decimais
            ticketMedio = faturamentoTotal.divide(new BigDecimal(totalPedidos), 2, RoundingMode.HALF_UP);
        }

        return new DashboardKpisDto(faturamentoTotal, totalPedidos, totalProdutosVendidos, ticketMedio);
    }

    // Pega o faturamento apenas da última semana para o gráfico de linhas
    public List<FaturamentoDiarioDto> obterFaturamentoDiario() {
        return vendaRepository.findFaturamentoDiarioByPeriodo(LocalDate.now().minusDays(7));
    }

    // Retorna os produtos mais populares
    public List<ProdutoRankingDto> obterTop5Produtos() {
        return produtoRepository.findTop5MaisVendidos();
    }

    // Retorna produtos com 0 ou contagem mínima de vendas nos últimos 15 dias
    public List<ProdutoRankingDto> obterProdutosBaixoGiro() {
        return produtoRepository.findProdutosComBaixoGiro(LocalDate.now().minusDays(15));
    }
}
