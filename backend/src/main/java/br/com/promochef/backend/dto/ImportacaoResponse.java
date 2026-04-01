package br.com.promochef.backend.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportacaoResponse {

    private boolean sucesso;
    private int totalLinhas;
    private int linhasSucesso;
    private int linhasErro;
    private String mensagem;
    private List<ImportacaoLinhaErro> erros;
}