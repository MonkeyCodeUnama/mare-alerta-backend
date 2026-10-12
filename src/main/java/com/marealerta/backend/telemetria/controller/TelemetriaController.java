package com.marealerta.backend.telemetria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marealerta.backend.telemetria.dto.LeituraTelemetriaResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.dto.TelemetriaPontoGraficoDTO;
import com.marealerta.backend.telemetria.service.TelemetriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
        return ResponseEntity.ok(telemetriaService.obterDashboard());
    }

    @Operation(summary = "Obter série temporal de 24 horas para o gráfico do painel")
    @ApiResponse(responseCode = "200", description = "Pontos do gráfico carregados com sucesso")
    @GetMapping("/grafico")
    public ResponseEntity<List<TelemetriaPontoGraficoDTO>> obterPontosGrafico() {
        return ResponseEntity.ok(telemetriaService.obterPontosGrafico());
    }

    @Operation(summary = "Sincronizar dados meteorológicos da Open-Meteo manualmente")
    @ApiResponse(responseCode = "200", description = "Sincronização executada com sucesso")
    @PostMapping("/sincronizar")
    public ResponseEntity<LeituraTelemetriaResponse> sincronizar() {
        return ResponseEntity.ok(telemetriaService.sincronizarOpenMeteo());
    }

    @Operation(summary = "Obter histórico recente de leituras da estação")
    @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    @GetMapping("/historico")
    public ResponseEntity<List<LeituraTelemetriaResponse>> obterHistorico(
            @RequestParam(required = false) String estacaoCodigo) {
        return ResponseEntity.ok(telemetriaService.obterHistoricoRecente(estacaoCodigo));
    }
}