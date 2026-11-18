package com.marealerta.backend.risco.controller;

import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.service.AlertaService;
import com.marealerta.backend.checklist.dto.ChecklistResumoDTO;
import com.marealerta.backend.checklist.service.ChecklistService;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.service.EstabelecimentoService;
import com.marealerta.backend.risco.dto.PainelResumoDTO;
import com.marealerta.backend.risco.dto.RiscoResponse;
import com.marealerta.backend.risco.service.RiscoService;
import com.marealerta.backend.telemetria.dto.TelemetriaDashboardResponse;
import com.marealerta.backend.telemetria.service.TelemetriaService;
import com.marealerta.backend.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/painel")
@RequiredArgsConstructor
@Tag(name = "Painel", description = "Endpoint consolidado para carregamento rápido do painel operacional")
public class PainelController {

    private final RiscoService riscoService;
    private final TelemetriaService telemetriaService;
    private final EstabelecimentoService estabelecimentoService;
    private final ChecklistService checklistService;
    private final AlertaService alertaService;

    @Operation(summary = "Obter resumo consolidado do painel", description = "Agrupa estabelecimento, risco atual do bairro, telemetria, resumo do checklist e último alerta em uma única chamada.")
    @ApiResponse(responseCode = "200", description = "Resumo do painel carregado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @GetMapping("/resumo")
    public ResponseEntity<PainelResumoDTO> obterResumo(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(name = "localidade", required = false) String localidade) {

        // Nunca expor dados globais (checklist agregado e ultimo alerta de qualquer
        // estabelecimento) para requisicoes anonimas.
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EstabelecimentoResponse estabelecimento = resolveEstabelecimento(usuario);
        String localidadeFinal = resolveLocalidade(localidade, estabelecimento);

        TelemetriaDashboardResponse dashboard = telemetriaService.obterDashboard();
        RiscoResponse risco = riscoService.processarEObterRiscoParaLocalidade(localidadeFinal);

        Long estabId = estabelecimento != null ? estabelecimento.getId() : null;
        ChecklistResumoDTO checklistResumo = checklistService.obterResumo(estabId);
        AlertaResponse ultimoAlerta = resolveUltimoAlerta(estabId);

        return ResponseEntity.ok(PainelResumoDTO.builder()
                .estabelecimento(estabelecimento)
                .risco(risco)
                .telemetriaDashboard(dashboard)
                .checklistResumo(checklistResumo)
                .ultimoAlerta(ultimoAlerta)
                .build());
    }

    private EstabelecimentoResponse resolveEstabelecimento(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            try {
                return estabelecimentoService.buscarPorEmailUsuario(usuario.getEmail());
            } catch (Exception ignored) {
            }
        }
        if (usuario.getEstabelecimento() != null && usuario.getEstabelecimento().getId() != null) {
            try {
                return estabelecimentoService.buscarPorId(usuario.getEstabelecimento().getId());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String resolveLocalidade(String localidade, EstabelecimentoResponse estabelecimento) {
        if (localidade != null && !localidade.isBlank()) {
            return localidade;
        }
        if (estabelecimento != null && estabelecimento.getBairro() != null) {
            return estabelecimento.getBairro().name();
        }
        return "BELEM_CENTRO";
    }

    private AlertaResponse resolveUltimoAlerta(Long estabelecimentoId) {
        if (estabelecimentoId != null) {
            List<AlertaResponse> lista = alertaService.listarPorEstabelecimento(estabelecimentoId);
            return (lista != null && !lista.isEmpty()) ? lista.get(0) : null;
        }
        List<AlertaResponse> gerais = alertaService.listarTodos();
        return (gerais != null && !gerais.isEmpty()) ? gerais.get(0) : null;
    }
}
