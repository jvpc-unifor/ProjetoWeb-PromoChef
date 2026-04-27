package br.com.promochef.backend.controllers;

import br.com.promochef.backend.models.Alerta;
import br.com.promochef.backend.models.TipoAlerta;
import br.com.promochef.backend.services.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    // Retorna os alertas de vencimento que ainda não foram visualizados
    @GetMapping("/vencimento")
    public ResponseEntity<List<Alerta>> getAlertasVencimento() {
        List<Alerta> alertas = alertaService.getAlertasNaoVisualizados(TipoAlerta.VENCIMENTO);
        return ResponseEntity.ok(alertas);
    }

    // Marca o alerta como visualizado para sumir do painel
    @PatchMapping("/{id}/visualizar")
    public ResponseEntity<Void> visualizarAlerta(@PathVariable Long id) {
        alertaService.marcarComoVisualizado(id);
        return ResponseEntity.ok().build();
    }
}
