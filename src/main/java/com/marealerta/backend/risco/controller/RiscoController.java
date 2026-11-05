package com.marealerta.backend.risco.controller;

import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.service.RiscoService;
import com.marealerta.backend.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Operation(summary = "Obter risco atual", description = "Calcula o nível de risco de alagamento para a localidade informada ou para o bairro do estabelecimento autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Análise de risco calculada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao processar dados de telemetria ou comunicação com serviço ML")
    })
    @GetMapping("/atual")
    public ResponseEntity<RiscoResponse> consultarRiscoAtual(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Identificador da localidade ou bairro monitorado", example = "JURUNAS")
            @RequestParam(name = "localidade", required = false) String localidade) {

        String localidadeFinal = localidade;
        if (localidadeFinal == null || localidadeFinal.isBlank()) {
            if (usuario != null && usuario.getEstabelecimento() != null && usuario.getEstabelecimento().getBairro() != null) {
                localidadeFinal = usuario.getEstabelecimento().getBairro().name();
            } else {
                localidadeFinal = "BELEM_CENTRO";
            }
        }

        RiscoResponse response = riscoService.processarEObterRiscoParaLocalidade(localidadeFinal);
        return ResponseEntity.ok(response);
    }
}
