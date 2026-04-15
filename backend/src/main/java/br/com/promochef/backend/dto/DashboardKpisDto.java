package br.com.promochef.backend.dto;

import java.math.BigDecimal;

public record DashboardKpisDto(
        BigDecimal faturamentoTotal,
        Integer totalPedidos,
        Integer totalProdutosVendidos,
        BigDecimal ticketMedio
) {}
