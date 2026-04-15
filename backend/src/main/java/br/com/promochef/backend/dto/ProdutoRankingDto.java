package br.com.promochef.backend.dto;

import java.math.BigDecimal;

public interface ProdutoRankingDto {
    Long getId();
    String getNome();
    Integer getTotalVendido();
    BigDecimal getReceitaTotal();
}
