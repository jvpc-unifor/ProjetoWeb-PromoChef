package br.com.promochef.backend.etl.transformers

import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DataTransformer {

    fun normalizeText(value: String?): String {
        return value?.trim() ?: ""
    }

    fun parseToDecimal(value: String?): BigDecimal? {
        if (value.isNullOrBlank()) return null
        
        return try {
            // Substitui vírgulas por pontos se o CSV vier em outro padrão numérico
            val normalizedValue = value.replace(",", ".").trim()
            BigDecimal(normalizedValue)
        } catch (e: NumberFormatException) {
            null // O chamador vai identificar isso como dado inconsistente
        }
    }

    fun validateRequiredColumns(record: Map<String, String>, requiredColumns: List<String>): List<String> {
        val missingColumns = mutableListOf<String>()
        
        requiredColumns.forEach { colName ->
            val fieldValue = record[colName]?.trim()
            if (fieldValue.isNullOrEmpty()) {
                missingColumns.add(colName)
            }
        }
        
        return missingColumns
    }
}
