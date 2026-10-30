package com.marealerta.backend.telemetria.controller;

import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.service.TelemetriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/telemetria")
@RequiredArgsConstructor
@Tag(name = "Telemetria", description = "Endpoints para monitoramento ambiental de marés e chuva em tempo real")
public class TelemetriaController {

    private final TelemetriaService telemetriaService;

    @Operation(summary = "Obter dados do dashboard", description = "Retorna os dados consolidados das estações de leitura (temperatura, vento, chuva e nível do rio).")
    @ApiResponse(responseCode = "200", description = "Dashboard carregado com sucesso")
    @GetMapping("/dashboard")
    public ResponseEntity<TelemetriaDashboardResponse> obterDashboard() {
        TelemetriaDashboardResponse dashboard = telemetriaService.obterDashboard();
        return ResponseEntity.ok(dashboard);
    }
}