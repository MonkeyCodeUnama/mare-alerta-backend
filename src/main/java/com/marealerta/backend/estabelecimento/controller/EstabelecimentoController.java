package com.marealerta.backend.estabelecimento.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoAtualizacaoRequest;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.service.EstabelecimentoService;
import com.marealerta.backend.usuario.model.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/estabelecimento", "/estabelecimentos"})
@RequiredArgsConstructor
@Tag(name = "Estabelecimento", description = "Endpoints de gerenciamento e perfil do estabelecimento comercial")
@SecurityRequirement(name = "bearerAuth")
public class EstabelecimentoController {

    private final EstabelecimentoService estabelecimentoService;

    @Operation(summary = "Buscar perfil do estabelecimento do usuário autenticado")
    @GetMapping("/me")
    public ResponseEntity<EstabelecimentoResponse> buscarMeuEstabelecimento(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(estabelecimentoService.buscarPorEmailUsuario(principal.getName()));
    }

    @Operation(summary = "Buscar estabelecimento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoResponse> buscarPorId(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        if (usuario != null && usuario.getEstabelecimento() != null) {
            return ResponseEntity.ok(estabelecimentoService.buscarPorIdDoUsuario(usuario, id));
        }
        return ResponseEntity.ok(estabelecimentoService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar perfil do estabelecimento (Etapa 2 do cadastro)")
    @PutMapping("/{id}")
    public ResponseEntity<EstabelecimentoResponse> atualizar(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id,
            @RequestBody EstabelecimentoAtualizacaoRequest request) {
        if (usuario != null && usuario.getEstabelecimento() != null) {
            return ResponseEntity.ok(estabelecimentoService.atualizarDoUsuario(usuario, id, request));
        }
        return ResponseEntity.ok(estabelecimentoService.atualizar(id, request));
    }

    @Operation(summary = "Listar todos os estabelecimentos ativos")
    @GetMapping
    public ResponseEntity<List<EstabelecimentoResponse>> listar(@AuthenticationPrincipal Usuario usuario) {
        if (usuario != null && usuario.getEmail() != null) {
            return ResponseEntity.ok(List.of(estabelecimentoService.buscarPorEmailUsuario(usuario.getEmail())));
        }
        return ResponseEntity.ok(estabelecimentoService.listarTodos());
    }
}
