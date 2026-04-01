package br.com.promochef.backend.services;

import br.com.promochef.backend.dto.ImportacaoLinhaErro;
import br.com.promochef.backend.dto.ImportacaoResponse;
import br.com.promochef.backend.models.Ingrediente;
import br.com.promochef.backend.models.Lote;
import br.com.promochef.backend.models.Produto;
import br.com.promochef.backend.repositories.IngredienteRepository;
import br.com.promochef.backend.repositories.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ImportacaoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public ImportacaoResponse importarProdutos(MultipartFile arquivo) {
        List<ImportacaoLinhaErro> erros = new ArrayList<>();
        int totalLinhas = 0;
        int linhasSucesso = 0;

        try (Reader reader = new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                totalLinhas++;
                long numeroLinha = record.getRecordNumber() + 1; // +1 por causa do header

                try {
                    String nome = getValor(record, "nome");
                    if (nome == null || nome.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "nome", getValor(record, "nome"), "Campo obrigatório"));
                        continue;
                    }

                    String precoStr = getValor(record, "preco");
                    if (precoStr == null || precoStr.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "preco", precoStr, "Campo obrigatório"));
                        continue;
                    }

                    BigDecimal preco;
                    try {
                        preco = new BigDecimal(precoStr.replace(",", "."));
                    } catch (NumberFormatException e) {
                        erros.add(criarErro(numeroLinha, "preco", precoStr, "Formato inválido (use 10.50 ou 10,50)"));
                        continue;
                    }

                    String categoria = getValor(record, "categoria");
                    if (categoria == null || categoria.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "categoria", categoria, "Campo obrigatório"));
                        continue;
                    }

                    // Criar e salvar produto
                    Produto produto = new Produto();
                    produto.setNome(nome.trim());
                    produto.setDescricao(getValor(record, "descricao"));
                    produto.setPreco(preco);
                    produto.setCategoria(categoria.trim());
                    produto.setAtivo(true);

                    produtoRepository.save(produto);
                    linhasSucesso++;

                } catch (Exception e) {
                    log.error("Erro ao importar produto na linha {}", numeroLinha, e);
                    erros.add(criarErro(numeroLinha, "geral", "", "Erro interno: " + e.getMessage()));
                }
            }

        } catch (Exception e) {
            log.error("Erro ao processar arquivo CSV de produtos", e);
            return ImportacaoResponse.builder()
                    .sucesso(false)
                    .totalLinhas(totalLinhas)
                    .linhasSucesso(0)
                    .linhasErro(1)
                    .mensagem("Erro ao processar arquivo: " + e.getMessage())
                    .erros(erros)
                    .build();
        }

        boolean sucesso = erros.isEmpty();
        return ImportacaoResponse.builder()
                .sucesso(sucesso)
                .totalLinhas(totalLinhas)
                .linhasSucesso(linhasSucesso)
                .linhasErro(erros.size())
                .mensagem(sucesso ? "Importação com sucesso!" : "Importação com erros")
                .erros(erros)
                .build();
    }

    @Transactional
    public ImportacaoResponse importarIngredientes(MultipartFile arquivo) {
        List<ImportacaoLinhaErro> erros = new ArrayList<>();
        int totalLinhas = 0;
        int linhasSucesso = 0;

        try (Reader reader = new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                totalLinhas++;
                long numeroLinha = record.getRecordNumber() + 1;

                try {
                    // Validação de campos obrigatórios
                    String nomeIngrediente = getValor(record, "nome_ingrediente");
                    if (nomeIngrediente == null || nomeIngrediente.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "nome_ingrediente", getValor(record, "nome_ingrediente"), "Campo obrigatório"));
                        continue;
                    }

                    String unidade = getValor(record, "unidade");
                    if (unidade == null || unidade.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "unidade", unidade, "Campo obrigatório"));
                        continue;
                    }

                    String quantidadeStr = getValor(record, "quantidade");
                    if (quantidadeStr == null || quantidadeStr.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "quantidade", quantidadeStr, "Campo obrigatório"));
                        continue;
                    }

                    String custoStr = getValor(record, "custo_unitario");
                    if (custoStr == null || custoStr.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "custo_unitario", custoStr, "Campo obrigatório"));
                        continue;
                    }

                    String dataValidadeStr = getValor(record, "data_validade");
                    if (dataValidadeStr == null || dataValidadeStr.trim().isEmpty()) {
                        erros.add(criarErro(numeroLinha, "data_validade", dataValidadeStr, "Campo obrigatório"));
                        continue;
                    }

                    BigDecimal quantidade;
                    try {
                        quantidade = new BigDecimal(quantidadeStr.replace(",", "."));
                    } catch (NumberFormatException e) {
                        erros.add(criarErro(numeroLinha, "quantidade", quantidadeStr, "Formato inválido"));
                        continue;
                    }

                    BigDecimal custoUnitario;
                    try {
                        custoUnitario = new BigDecimal(custoStr.replace(",", "."));
                    } catch (NumberFormatException e) {
                        erros.add(criarErro(numeroLinha, "custo_unitario", custoStr, "Formato inválido"));
                        continue;
                    }

                    // Parse da data
                    LocalDate dataValidade;
                    try {
                        dataValidade = LocalDate.parse(dataValidadeStr, DATE_FORMAT);
                    } catch (DateTimeParseException e) {
                        erros.add(criarErro(numeroLinha, "data_validade", dataValidadeStr, "Formato inválido (use AAAA-MM-DD)"));
                        continue;
                    }

                    // Buscar ou criar ingrediente
                    Ingrediente ingrediente = ingredienteRepository.findByNome(nomeIngrediente.trim())
                            .orElseGet(() -> {
                                Ingrediente novoIngrediente = new Ingrediente();
                                novoIngrediente.setNome(nomeIngrediente.trim());
                                novoIngrediente.setUnidade(unidade.trim());
                                novoIngrediente.setEstoqueMinimo(new BigDecimal("0"));
                                return ingredienteRepository.save(novoIngrediente);
                            });

                    // Criar lote
                    Lote lote = new Lote();
                    lote.setIngrediente(ingrediente);
                    lote.setQuantidade(quantidade);
                    lote.setCustoUnitario(custoUnitario);
                    lote.setDataValidade(dataValidade);
                    lote.setDataEntrada(LocalDate.now());
                    lote.setNumeroLote(getValor(record, "numero_lote"));
                    lote.setObservacao(getValor(record, "observacao"));

                    ingredienteRepository.saveLote(lote);
                    linhasSucesso++;

                } catch (Exception e) {
                    log.error("Erro ao importar ingrediente na linha {}", numeroLinha, e);
                    erros.add(criarErro(numeroLinha, "geral", "", "Erro interno: " + e.getMessage()));
                }
            }

        } catch (Exception e) {
            log.error("Erro ao processar arquivo CSV de ingredientes", e);
            return ImportacaoResponse.builder()
                    .sucesso(false)
                    .totalLinhas(totalLinhas)
                    .linhasSucesso(0)
                    .linhasErro(1)
                    .mensagem("Erro ao processar arquivo: " + e.getMessage())
                    .erros(erros)
                    .build();
        }

        boolean sucesso = erros.isEmpty();
        return ImportacaoResponse.builder()
                .sucesso(sucesso)
                .totalLinhas(totalLinhas)
                .linhasSucesso(linhasSucesso)
                .linhasErro(erros.size())
                .mensagem(sucesso ? "Importação com sucesso!" : "Importação com erros")
                .erros(erros)
                .build();
    }

    //
    private String getValor(CSVRecord record, String coluna) {
        try {
            return record.get(coluna);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ImportacaoLinhaErro criarErro(long linha, String campo, String valor, String motivo) {
        return ImportacaoLinhaErro.builder()
                .numeroLinha((int) linha)
                .campo(campo)
                .valor(valor != null ? valor : "")
                .motivo(motivo)
                .build();
    }
}