package br.com.promochef.backend.etl.extractors

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.sql.DriverManager

@Component
class PdvExtractor {

    @Value("\${pdv.datasource.url:jdbc:mysql://localhost:3306/pdv_ficticio?serverTimezone=UTC&useSSL=false}")
    private lateinit var pdvUrl: String

    @Value("\${pdv.datasource.username:root}")
    private lateinit var pdvUsername: String

    @Value("\${pdv.datasource.password:root}")
    private lateinit var pdvPassword: String

    fun extract(sqlQuery: String): List<Map<String, String>> {
        val extracao = mutableListOf<Map<String, String>>()
        
        try {
            //bre a conexão apenas para buscar os dados e já fecha o driver
            val connection = DriverManager.getConnection(pdvUrl, pdvUsername, pdvPassword)
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(sqlQuery)
            
            //Extrai os metadados para descobrir quais colunas voltaram na consulta
            val metaData = resultSet.metaData
            val totalColunas = metaData.columnCount

            //Itera linha por linha
            while (resultSet.next()) {
                val linhaAtual = mutableMapOf<String, String>()

                for (indiceColuna in 1..totalColunas) {
                    val nomeDaColuna = metaData.getColumnName(indiceColuna)
                    
                    // Converte pra String
                    val valorDaColuna = resultSet.getString(indiceColuna) ?: ""
                    linhaAtual[nomeDaColuna] = valorDaColuna
                }
                
                extracao.add(linhaAtual)
            }

            resultSet.close()
            statement.close()
            connection.close() 

        } catch (erro: Exception) {
            throw RuntimeException("Falha ao extrair dados do fluxo PDV: ${erro.message}", erro)
        }
        
        return extracao
    }
}
