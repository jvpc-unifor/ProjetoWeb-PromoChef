package br.com.promochef.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FaturamentoDiarioDto {
    LocalDate getDataVenda();
    BigDecimal getFaturamento();
    Integer getTotalPedidos();
}
