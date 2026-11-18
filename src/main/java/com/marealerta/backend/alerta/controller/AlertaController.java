package com.marealerta.backend.alerta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.marealerta.backend.alerta.dto.AlertaBairroRequest;
import com.marealerta.backend.alerta.dto.AlertaRequest;
import com.marealerta.backend.alerta.dto.AlertaResponse;
import com.marealerta.backend.alerta.model.EnvioAlertaWhatsapp;
import com.marealerta.backend.alerta.service.AlertaService;
import com.marealerta.backend.usuario.model.Role;
import com.marealerta.backend.usuario.model.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas e notificações via WhatsApp")
public class AlertaController {

    private final AlertaService alertaService;

    @Operation(summary = "Listar alertas", description = "Retorna histórico de alertas emitidos. Lojistas autenticados visualizam apenas os alertas do próprio estabelecimento.")
    @ApiResponse(responseCode = "200", description = "Lista de alertas retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) Long estabelecimentoId) {
        if (usuario != null && usuario.getRole() != Role.ADMIN) {
            if (usuario.getEstabelecimento() == null || usuario.getEstabelecimento().getId() == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(alertaService.listarPorEstabelecimento(usuario.getEstabelecimento().getId()));
        }
        if (estabelecimentoId != null) {
            return ResponseEntity.ok(alertaService.listarPorEstabelecimento(estabelecimentoId));
        }
        return ResponseEntity.ok(alertaService.listarTodos());
    }

    @Operation(summary = "Criar alerta", description = "Registra um alerta para um estabelecimento e despacha notificação via WhatsApp se houver telefone.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Alerta criado com sucesso")
    @PostMapping
    public ResponseEntity<AlertaResponse> criar(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AlertaRequest request) {
        AlertaResponse response;
        if (usuario != null && usuario.getEstabelecimento() != null) {
            response = alertaService.criar(usuario, request);
        } else {
            response = alertaService.criarAlerta(request);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Disparar alertas por bairro", description = "Emite alerta para todos os estabelecimentos de um bairro e notifica via WhatsApp (Admin).")
    @ApiResponse(responseCode = "201", description = "Alertas do bairro disparados com sucesso")
    @PostMapping("/bairro")
    public ResponseEntity<List<AlertaResponse>> dispararParaBairro(@Valid @RequestBody AlertaBairroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertaService.dispararAlertaBairro(
                request.getBairro(),
                request.getNivelRisco(),
                request.getMensagem(),
                request.getTipoAlerta(),
                request.getOrigemAlerta()
        ));
    }

    @Operation(summary = "Obter detalhe do alerta", description = "Retorna informações detalhadas de um alerta pelo ID.")
    @ApiResponse(responseCode = "200", description = "Detalhes do alerta retornados com sucesso")
    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> verDetalhe(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id) {
        AlertaResponse alerta = alertaService.buscarPorId(id);
        validarAcessoAlerta(usuario, alerta);
        return ResponseEntity.ok(alerta);
    }

    @Operation(summary = "Listar envios de WhatsApp do alerta", description = "Retorna o status de rastreio e entrega do WhatsApp para o alerta.")
    @ApiResponse(responseCode = "200", description = "Histórico de despachos retornado com sucesso")
    @GetMapping("/{id}/envios-whatsapp")
    public ResponseEntity<List<EnvioAlertaWhatsapp>> listarEnviosWhatsapp(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id) {
        if (usuario != null && usuario.getRole() != Role.ADMIN) {
            AlertaResponse alerta = alertaService.buscarPorId(id);
            validarAcessoAlerta(usuario, alerta);
        }
        return ResponseEntity.ok(alertaService.listarEnviosWhatsapp(id));
    }

    private void validarAcessoAlerta(Usuario usuario, AlertaResponse alerta) {
        if (usuario == null || usuario.getRole() == Role.ADMIN) {
            return;
        }
        Long estabUsuario = usuario.getEstabelecimento() != null ? usuario.getEstabelecimento().getId() : null;
        if (estabUsuario == null || (alerta != null && !estabUsuario.equals(alerta.getEstabelecimentoId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "sem permissão para acessar este alerta");
        }
    }
}
