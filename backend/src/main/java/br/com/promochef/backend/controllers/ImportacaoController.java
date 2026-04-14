package br.com.promochef.backend.controllers;

import br.com.promochef.backend.dto.ImportacaoResponse;
import br.com.promochef.backend.services.ImportacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/importacao")
@CrossOrigin(origins = "*")
public class ImportacaoController {

    @Autowired
    private ImportacaoService importacaoService;

    @PostMapping(value = "/produtos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ImportacaoResponse> importarProdutos(
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {

        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ImportacaoResponse.builder()
                            .sucesso(false)
                            .mensagem("Arquivo não pode estar vazio")
                            .build()
            );
        }

        if (!arquivo.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                    ImportacaoResponse.builder()
                            .sucesso(false)
                            .mensagem("Apenas arquivos CSV são permitidos")
                            .build()
            );
        }

        ImportacaoResponse response = importacaoService.importarProdutos(arquivo);

        if (response.isSucesso()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping(value = "/ingredientes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ImportacaoResponse> importarIngredientes(
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {

        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ImportacaoResponse.builder()
                            .sucesso(false)
                            .mensagem("Arquivo não pode estar vazio")
                            .build()
            );
        }

        if (!arquivo.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                    ImportacaoResponse.builder()
                            .sucesso(false)
                            .mensagem("Apenas arquivos CSV são permitidos")
                            .build()
            );
        }

        ImportacaoResponse response = importacaoService.importarIngredientes(arquivo);

        if (response.isSucesso()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping(value = "/ficha-tecnica", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ImportacaoResponse> importarFichaTecnica(
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {

        if (arquivo.isEmpty() || !arquivo.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                    ImportacaoResponse.builder()
                            .sucesso(false)
                            .mensagem("Arquivo não pode estar vazio e deve ser do tipo CSV.")
                            .build()
            );
        }

        ImportacaoResponse response = importacaoService.importarFichaTecnica(arquivo);
        return response.isSucesso() ? ResponseEntity.ok(response) : ResponseEntity.status(400).body(response);
    }

    @PostMapping(value = "/vendas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ImportacaoResponse> importarVendas(
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {

        if (arquivo.isEmpty() || !arquivo.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                    ImportacaoResponse.builder()
                            .sucesso(false)
                            .mensagem("Arquivo não pode estar vazio e deve ser do tipo CSV.")
                            .build()
            );
        }

        ImportacaoResponse response = importacaoService.importarVendas(arquivo);
        return response.isSucesso() ? ResponseEntity.ok(response) : ResponseEntity.status(400).body(response);
    }

    @PostMapping(value = "/pdv")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ImportacaoResponse> importarPdv() {
        ImportacaoResponse response = importacaoService.importarDadosDoPdv();
        if (response.isSucesso()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body(response);
        }
    }
}
