package br.com.promochef.backend.services;

import br.com.promochef.backend.dto.ImportacaoLinhaErro;
import br.com.promochef.backend.dto.ImportacaoResponse;
import br.com.promochef.backend.etl.extractors.CsvExtractor;
import br.com.promochef.backend.etl.transformers.DataTransformer;
import br.com.promochef.backend.models.Ingrediente;
import br.com.promochef.backend.models.Lote;
import br.com.promochef.backend.models.Produto;
import br.com.promochef.backend.repositories.IngredienteRepository;
import br.com.promochef.backend.repositories.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ImportacaoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private CsvExtractor csvExtractor;

    @Autowired
    private DataTransformer dataTransformer;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public ImportacaoResponse importarProdutos(MultipartFile arquivo) {
        List<ImportacaoLinhaErro> erros = new ArrayList<>();
        int totalLinhas = 0;
        int linhasSucesso = 0;

        try {
            List<Map<String, String>> registros = csvExtractor.extract(arquivo.getInputStream());
            totalLinhas = registros.size();

            for (int i = 0; i < registros.size(); i++) {
                Map<String, String> record = registros.get(i);
                long numeroLinha = i + 2; // +1 zero-index, +1 header

                try {
                    String nome = dataTransformer.normalizeText(record.get("nome"));
                    if (nome.isEmpty()) {
                        erros.add(criarErro(numeroLinha, "nome", "", "Campo obrigatório"));
                        continue;
                    }

                    String precoStr = record.get("preco");
                    BigDecimal preco = dataTransformer.parseToDecimal(precoStr);
                    if (preco == null) {
                        erros.add(criarErro(numeroLinha, "preco", precoStr, "Formato inválido ou vazio"));
                        continue;
                    }

                    String categoria = dataTransformer.normalizeText(record.get("categoria"));
                    if (categoria.isEmpty()) {
                        erros.add(criarErro(numeroLinha, "categoria", "", "Campo obrigatório"));
                        continue;
                    }

                    // Criar e salvar produto
                    Produto produto = new Produto();
                    produto.setNome(nome);
                    produto.setDescricao(dataTransformer.normalizeText(record.get("descricao")));
                    produto.setPreco(preco);
                    produto.setCategoria(categoria);
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
                    .mensagem("Erro ao ler CSV: " + e.getMessage())
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

        try {
            List<Map<String, String>> registros = csvExtractor.extract(arquivo.getInputStream());
            totalLinhas = registros.size();

            for (int i = 0; i < registros.size(); i++) {
                Map<String, String> record = registros.get(i);
                long numeroLinha = i + 2;

                try {
                    String nomeIngrediente = dataTransformer.normalizeText(record.get("nome_ingrediente"));
                    if (nomeIngrediente.isEmpty()) {
                        erros.add(criarErro(numeroLinha, "nome_ingrediente", "", "Campo obrigatório"));
                        continue;
                    }

                    String unidade = dataTransformer.normalizeText(record.get("unidade"));
                    if (unidade.isEmpty()) {
                        erros.add(criarErro(numeroLinha, "unidade", "", "Campo obrigatório"));
                        continue;
                    }

                    String quantidadeStr = record.get("quantidade");
                    BigDecimal quantidade = dataTransformer.parseToDecimal(quantidadeStr);
                    if (quantidade == null) {
                        erros.add(criarErro(numeroLinha, "quantidade", quantidadeStr, "Formato numérico inválido"));
                        continue;
                    }

                    String custoStr = record.get("custo_unitario");
                    BigDecimal custoUnitario = dataTransformer.parseToDecimal(custoStr);
                    if (custoUnitario == null) {
                        erros.add(criarErro(numeroLinha, "custo_unitario", custoStr, "Formato numérico inválido"));
                        continue;
                    }

                    String dataValidadeStr = record.get("data_validade");
                    LocalDate dataValidade;
                    try {
                        dataValidade = LocalDate.parse(dataTransformer.normalizeText(dataValidadeStr), DATE_FORMAT);
                    } catch (DateTimeParseException e) {
                        erros.add(criarErro(numeroLinha, "data_validade", dataValidadeStr, "Formato inválido (use AAAA-MM-DD)"));
                        continue;
                    }

                    // Buscar ou criar ingrediente
                    Ingrediente ingrediente = ingredienteRepository.findByNome(nomeIngrediente)
                            .orElseGet(() -> {
                                Ingrediente novoIngrediente = new Ingrediente();
                                novoIngrediente.setNome(nomeIngrediente);
                                novoIngrediente.setUnidade(unidade);
                                novoIngrediente.setEstoqueMinimo(BigDecimal.ZERO);
                                return ingredienteRepository.save(novoIngrediente);
                            });

                    // Criar lote
                    Lote lote = new Lote();
                    lote.setIngrediente(ingrediente);
                    lote.setQuantidade(quantidade);
                    lote.setCustoUnitario(custoUnitario);
                    lote.setDataValidade(dataValidade);
                    lote.setDataEntrada(LocalDate.now());
                    lote.setNumeroLote(dataTransformer.normalizeText(record.get("numero_lote")));
                    lote.setObservacao(dataTransformer.normalizeText(record.get("observacao")));

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

    private ImportacaoLinhaErro criarErro(long linha, String campo, String valor, String motivo) {
        return ImportacaoLinhaErro.builder()
                .numeroLinha((int) linha)
                .campo(campo)
                .valor(valor != null ? valor : "")
                .motivo(motivo)
                .build();
    }
}
