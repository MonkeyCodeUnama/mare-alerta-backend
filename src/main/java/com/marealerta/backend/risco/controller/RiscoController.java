package com.marealerta.backend.risco.controller;

import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.service.RiscoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risco")
@RequiredArgsConstructor
@Tag(name = "Análise de Risco", description = "Endpoints para consulta de predição de alagamento via Machine Learning")
public class RiscoController {

    private final RiscoService riscoService;

    @Operation(summary = "Obter risco atual", description = "Calcula o nível de risco de alagamento para a localidade com base na telemetria atual e no modelo de ML.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Análise de risco calculada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao processar dados de telemetria ou comunicação com serviço ML")
    })
    @GetMapping("/atual")
    public ResponseEntity<RiscoResponse> consultarRiscoAtual(
            @Parameter(description = "Identificador da localidade ou estação monitorada", example = "BELEM_CENTRO")
            @RequestParam(name = "localidade", defaultValue = "BELEM_CENTRO") String localidade) {

        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade(localidade);
        return ResponseEntity.ok(response);
    }
}