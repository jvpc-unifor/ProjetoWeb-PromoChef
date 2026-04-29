package br.com.promochef.backend.controllers;

import br.com.promochef.backend.models.Promocao;
import br.com.promochef.backend.services.MotorPromocaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promocoes")
@RequiredArgsConstructor
public class PromocaoController {

    private final MotorPromocaoService motorPromocaoService;

    @GetMapping("/sugestoes")
    public ResponseEntity<List<Promocao>> getSugestoes() {
        return ResponseEntity.ok(motorPromocaoService.listarSugestoes());
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        motorPromocaoService.ativarPromocao(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<Void> recusar(@PathVariable Long id) {
        motorPromocaoService.recusarPromocao(id);
        return ResponseEntity.ok().build();
    }
}
