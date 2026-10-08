package com.marealerta.backend.alerta.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.service.AlertaService;
import com.marealerta.backend.usuario.model.Usuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listar() {
        return ResponseEntity.ok(alertaService.listar());
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Alerta criado")
    public ResponseEntity<AlertaResponse> criar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody @Valid AlertaRequest request) {
        AlertaResponse response = alertaService.criar(usuario, request);
        return ResponseEntity.created(URI.create("/alertas/" + response.getId())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> verDetalhe(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.buscar(id));
    }
}
