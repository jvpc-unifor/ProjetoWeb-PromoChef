package br.com.promochef.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportacaoLinhaErro {

    private int numeroLinha;
    private String campo;
    private String valor;
    private String motivo;
}