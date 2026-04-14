package br.com.promochef.backend.etl.extractors

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@Component
class CsvExtractor {

    fun extract(inputStream: InputStream): List<Map<String, String>> {
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .build()
            
        val parser = CSVParser(reader, csvFormat)
        val extractedData = mutableListOf<Map<String, String>>()
        
        for (record in parser) {
            extractedData.add(record.toMap())
        }
        
        parser.close()
        reader.close()
        
        return extractedData
    }
}