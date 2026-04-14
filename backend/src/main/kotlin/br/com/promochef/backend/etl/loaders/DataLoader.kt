package br.com.promochef.backend.etl.loaders

import br.com.promochef.backend.models.*
import br.com.promochef.backend.repositories.*
import org.springframework.stereotype.Component

/**
 * Componente final do pipeline ETL.
 * Responsável exclusivo por gravar os dados higienizados nas respectivas tabelas do banco de dados PromoChef.
 */
@Component
class DataLoader(
    private val produtoRepository: ProdutoRepository,
    private val ingredienteRepository: IngredienteRepository,
    private val loteRepository: LoteRepository,
    private val fichaTecnicaRepository: FichaTecnicaRepository,
    private val vendaRepository: VendaRepository
) {
    fun loadProduto(produto: Produto): Produto {
        return produtoRepository.save(produto)
    }

    fun loadIngrediente(ingrediente: Ingrediente): Ingrediente {
        return ingredienteRepository.save(ingrediente)
    }

    fun loadLote(lote: Lote): Lote {
        return loteRepository.save(lote)
    }

    fun loadFichaTecnica(fichaTecnica: FichaTecnica): FichaTecnica {
        return fichaTecnicaRepository.save(fichaTecnica)
    }

    fun loadVenda(venda: Venda): Venda {
        return vendaRepository.save(venda)
    }

    fun getProdutoByNome(nome: String): Produto? {
        return produtoRepository.findByNome(nome).orElse(null)
    }

    fun getIngredienteByNome(nome: String): Ingrediente? {
        return ingredienteRepository.findByNome(nome).orElse(null)
    }
}
