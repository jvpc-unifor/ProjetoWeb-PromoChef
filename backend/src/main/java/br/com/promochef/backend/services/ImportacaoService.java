package br.com.promochef.backend.services;

import br.com.promochef.backend.dto.ImportacaoLinhaErro;
import br.com.promochef.backend.dto.ImportacaoResponse;
import br.com.promochef.backend.etl.extractors.CsvExtractor;
import br.com.promochef.backend.etl.extractors.PdvExtractor;
import br.com.promochef.backend.etl.transformers.DataTransformer;
import br.com.promochef.backend.models.Ingrediente;
import br.com.promochef.backend.models.Lote;
import br.com.promochef.backend.models.Produto;
import br.com.promochef.backend.models.FichaTecnica;
import br.com.promochef.backend.models.FichaTecnicaId;
import br.com.promochef.backend.models.Venda;
import br.com.promochef.backend.models.ItemVenda;
import br.com.promochef.backend.etl.loaders.DataLoader;
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
    private DataLoader dataLoader;

    @Autowired
    private CsvExtractor csvExtractor;

    @Autowired
    private PdvExtractor pdvExtractor;

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

                    dataLoader.loadProduto(produto);
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
                    Ingrediente ingrediente = dataLoader.getIngredienteByNome(nomeIngrediente);
                    if (ingrediente == null) {
                        Ingrediente novoIngrediente = new Ingrediente();
                        novoIngrediente.setNome(nomeIngrediente);
                        novoIngrediente.setUnidade(unidade);
                        novoIngrediente.setEstoqueMinimo(BigDecimal.ZERO);
                        ingrediente = dataLoader.loadIngrediente(novoIngrediente);
                    }

                    // Criar lote
                    Lote lote = new Lote();
                    lote.setIngrediente(ingrediente);
                    lote.setQuantidade(quantidade);
                    lote.setCustoUnitario(custoUnitario);
                    lote.setDataValidade(dataValidade);
                    lote.setDataEntrada(LocalDate.now());
                    lote.setNumeroLote(dataTransformer.normalizeText(record.get("numero_lote")));
                    lote.setObservacao(dataTransformer.normalizeText(record.get("observacao")));

                    dataLoader.loadLote(lote);
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

    @Transactional
    public ImportacaoResponse importarFichaTecnica(MultipartFile arquivo) {
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
                    String nomeProduto = dataTransformer.normalizeText(record.get("nome_produto"));
                    String nomeIngrediente = dataTransformer.normalizeText(record.get("nome_ingrediente"));
                    String strQtd = record.get("quantidade");
                    String unidade = dataTransformer.normalizeText(record.get("unidade"));

                    if (nomeProduto.isEmpty()) {
                        erros.add(criarErro(numeroLinha, "nome_produto", "", "Campo obrigatório"));
                        continue;
                    }
                    if (nomeIngrediente.isEmpty()) {
                        erros.add(criarErro(numeroLinha, "nome_ingrediente", "", "Campo obrigatório"));
                        continue;
                    }
                    BigDecimal quantidade = dataTransformer.parseToDecimal(strQtd);
                    if (quantidade == null) {
                        erros.add(criarErro(numeroLinha, "quantidade", strQtd, "Formato numérico inválido"));
                        continue;
                    }
                    if (unidade.isEmpty()) {
                        erros.add(criarErro(numeroLinha, "unidade", "", "Campo obrigatório"));
                        continue;
                    }

                    Produto produto = dataLoader.getProdutoByNome(nomeProduto);
                    if (produto == null) {
                        erros.add(criarErro(numeroLinha, "nome_produto", nomeProduto, "Produto não encontrado"));
                        continue;
                    }

                    Ingrediente ingrediente = dataLoader.getIngredienteByNome(nomeIngrediente);
                    if (ingrediente == null) {
                        erros.add(criarErro(numeroLinha, "nome_ingrediente", nomeIngrediente, "Ingrediente não encontrado"));
                        continue;
                    }

                    FichaTecnicaId id = new FichaTecnicaId(produto.getId(), ingrediente.getId());
                    FichaTecnica ft = new FichaTecnica();
                    ft.setId(id);
                    ft.setProduto(produto);
                    ft.setIngrediente(ingrediente);
                    ft.setQuantidadeUsada(quantidade);
                    ft.setUnidade(unidade);

                    dataLoader.loadFichaTecnica(ft);
                    linhasSucesso++;
                } catch (Exception e) {
                    log.error("Erro na leitura da linha {}", numeroLinha, e);
                    erros.add(criarErro(numeroLinha, "geral", "", "Erro: " + e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Erro no import da ficha tecnica", e);
            erros.add(criarErro(1, "arquivo", null, e.getMessage()));
            return ImportacaoResponse.builder()
                    .sucesso(false).totalLinhas(totalLinhas).linhasErro(erros.size()).linhasSucesso(linhasSucesso)
                    .mensagem("Falha.").erros(erros).build();
        }

        return ImportacaoResponse.builder()
                .sucesso(erros.isEmpty()).totalLinhas(totalLinhas).linhasSucesso(linhasSucesso).linhasErro(erros.size())
                .mensagem(erros.isEmpty() ? "Sucesso" : "Foram encontrados erros").erros(erros).build();
    }

    @Transactional
    public ImportacaoResponse importarVendas(MultipartFile arquivo) {
        List<ImportacaoLinhaErro> erros = new ArrayList<>();
        int totalLinhas = 0;
        int linhasSucesso = 0;

        try {
            List<Map<String, String>> registros = csvExtractor.extract(arquivo.getInputStream());
            totalLinhas = registros.size();

            // Como as vendas agrupam itens, se vier 1 item por linha,
            // podemos agrupar, mas neste modelo simplificado criaremos
            // uma venda separada para cada linha caso falte uma lógica de ID de cupom.
            // Para simplicidade de demonstração, criamos uma nova Venda por registro.

            for (int i = 0; i < registros.size(); i++) {
                Map<String, String> record = registros.get(i);
                long numeroLinha = i + 2;

                try {
                    String nomeProduto = dataTransformer.normalizeText(record.get("nome_produto"));
                    String dataStr = dataTransformer.normalizeText(record.get("data_venda"));
                    String strQtd = record.get("quantidade");

                    if (nomeProduto.isEmpty() || dataStr.isEmpty() || strQtd == null) {
                        erros.add(criarErro(numeroLinha, "geral", "", "nome_produto, data_venda e quantidade são necessários"));
                        continue;
                    }

                    BigDecimal qtd = dataTransformer.parseToDecimal(strQtd);
                    if (qtd == null) {
                        erros.add(criarErro(numeroLinha, "quantidade", strQtd, "Quantidade inválida"));
                        continue;
                    }

                    LocalDate dataVal;
                    try {
                        dataVal = LocalDate.parse(dataStr, DATE_FORMAT);
                    } catch (DateTimeParseException e) {
                        erros.add(criarErro(numeroLinha, "data_venda", dataStr, "Data inválida"));
                        continue;
                    }

                    Produto produto = dataLoader.getProdutoByNome(nomeProduto);
                    if (produto == null) {
                        erros.add(criarErro(numeroLinha, "nome_produto", nomeProduto, "Produto não encontrado"));
                        continue;
                    }

                    Venda venda = new Venda();
                    venda.setDataVenda(dataVal);
                    // preço unitario da venda = o preço vigente do produto x quantidade
                    BigDecimal total = produto.getPreco().multiply(qtd);
                    venda.setValorTotal(total);

                    ItemVenda iv = new ItemVenda();
                    iv.setVenda(venda);
                    iv.setProduto(produto);
                    iv.setQuantidade(qtd.intValue());
                    iv.setPrecoUnitario(produto.getPreco());

                    venda.getItens().add(iv);
                    dataLoader.loadVenda(venda);

                    linhasSucesso++;
                } catch (Exception e) {
                    log.error("Erro na leitura da linha {}", numeroLinha, e);
                    erros.add(criarErro(numeroLinha, "geral", "", "Erro: " + e.getMessage()));
                }
            }

        } catch (Exception e) {
            log.error("Erro ao no import da venda", e);
            erros.add(criarErro(1, "arquivo", null, e.getMessage()));
            return ImportacaoResponse.builder()
                    .sucesso(false).totalLinhas(totalLinhas).linhasErro(erros.size()).linhasSucesso(linhasSucesso)
                    .mensagem("Falha.").erros(erros).build();
        }

        return ImportacaoResponse.builder()
                .sucesso(erros.isEmpty()).totalLinhas(totalLinhas).linhasSucesso(linhasSucesso).linhasErro(erros.size())
                .mensagem(erros.isEmpty() ? "Sucesso" : "Erros encontrados").erros(erros).build();
    }

    @Transactional
    public ImportacaoResponse importarDadosDoPdv() {
        List<ImportacaoLinhaErro> erros = new ArrayList<>();
        int totalLinhas = 0;
        int linhasSucesso = 0;

        try {
            List<Map<String, String>> produtosPdv = pdvExtractor.extract("SELECT * FROM pdv_produto");
            totalLinhas += produtosPdv.size();
            for (Map<String, String> record : produtosPdv) {
                try {
                    String nome = dataTransformer.normalizeText(record.get("nome"));
                    String precoStr = record.get("preco_venda");
                    BigDecimal preco = dataTransformer.parseToDecimal(precoStr);
                    String categoria = dataTransformer.normalizeText(record.get("categoria"));
                    
                    if (nome.isEmpty() || preco == null || categoria.isEmpty()) continue;

                    Produto produto = new Produto();
                    produto.setNome(nome);
                    produto.setPreco(preco);
                    produto.setCategoria(categoria);
                    produto.setAtivo(true);
                    dataLoader.loadProduto(produto);
                    linhasSucesso++;
                } catch (Exception e) {
                    log.error("Erro importando produto do PDV: ", e);
                }
            }

            // Extração de Vendas do PDV com JOIN para já trazer o nome
            List<Map<String, String>> vendasPdv = pdvExtractor.extract(
                "SELECT pv.*, p.nome as nome_produto FROM pdv_venda pv JOIN pdv_produto p ON pv.produto_id = p.id"
            );
            totalLinhas += vendasPdv.size();
            for (Map<String, String> record : vendasPdv) {
                try {
                    String nomeProduto = dataTransformer.normalizeText(record.get("nome_produto"));
                    String strQtd = record.get("quantidade");
                    BigDecimal qtd = dataTransformer.parseToDecimal(strQtd);
                    
                    String dataOriginal = record.get("data_venda");
                    if (dataOriginal != null && dataOriginal.length() >= 10) {
                        dataOriginal = dataOriginal.substring(0, 10);
                    }
                    
                    if (nomeProduto.isEmpty() || dataOriginal == null || qtd == null) continue;
                    
                    LocalDate dataVal;
                    try {
                        dataVal = LocalDate.parse(dataOriginal, DATE_FORMAT);
                    } catch (DateTimeParseException e) {
                        continue;
                    }

                    Produto produto = dataLoader.getProdutoByNome(nomeProduto);
                    if (produto == null) continue;

                    Venda venda = new Venda();
                    venda.setDataVenda(dataVal);
                    // O preço e a quantidade definem o valor total de forma padronizada
                    BigDecimal total = produto.getPreco().multiply(qtd);
                    venda.setValorTotal(total);

                    ItemVenda iv = new ItemVenda();
                    iv.setVenda(venda);
                    iv.setProduto(produto);
                    iv.setQuantidade(qtd.intValue());
                    iv.setPrecoUnitario(produto.getPreco());

                    venda.getItens().add(iv);
                    dataLoader.loadVenda(venda);
                    linhasSucesso++;
                } catch (Exception e) {
                    log.error("Erro importando venda do PDV: ", e);
                }
            }

        } catch (Exception e) {
            log.error("Erro geral na integração com PDV", e);
            erros.add(criarErro(1, "pdv", null, "Falha ao se conectar/extrair do PDV: " + e.getMessage()));
            return ImportacaoResponse.builder()
                    .sucesso(false).totalLinhas(totalLinhas).linhasErro(erros.size()).linhasSucesso(linhasSucesso)
                    .mensagem("Falha na integração com o PDV").erros(erros).build();
        }

        return ImportacaoResponse.builder()
                .sucesso(true).totalLinhas(totalLinhas).linhasSucesso(linhasSucesso).linhasErro(erros.size())
                .mensagem("Integração PDV finalizada com sucesso! Foram processados " + linhasSucesso + " registros.")
                .erros(erros).build();
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
