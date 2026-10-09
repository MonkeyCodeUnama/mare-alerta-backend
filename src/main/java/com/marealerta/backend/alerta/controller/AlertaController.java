package com.marealerta.backend.alerta.controller;

import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
@Tag(name = "Alerta", description = "Endpoints dos alertas do estabelecimento comercial")
public class AlertaController {
    private final AlertaService alertaService;

    @Operation(summary = "listar alertas")
    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listar(@RequestParam Long estabelecimentoId){
        List<AlertaResponse> alertas = alertaService.listarPorEstabelecimento(estabelecimentoId);
        return ResponseEntity.ok(alertas);
    }

    @Operation(summary = "criar um alerta")
    @PostMapping
    public ResponseEntity<AlertaResponse> criar(@RequestBody @Valid AlertaRequest alertaRequest) throws BadRequestException {
        AlertaResponse response = alertaService.enviarAlerta(alertaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(summary = "ver detalhes do alerta")
    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> verDetalhe(@PathVariable Long id){
        AlertaResponse response = alertaService.buscarPorId(id);
        return ResponseEntity.ok(response);

    }
}
