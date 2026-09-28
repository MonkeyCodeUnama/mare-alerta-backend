package com.marealerta.backend.estabelecimento.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marealerta.backend.estabelecimento.dto.EstabelecimentoAtualizacaoRequest;
import com.marealerta.backend.estabelecimento.dto.EstabelecimentoResponse;
import com.marealerta.backend.estabelecimento.service.EstabelecimentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estabelecimento")
@RequiredArgsConstructor
@Tag(name = "Estabelecimento", description = "Endpoints de gerenciamento e perfil do estabelecimento comercial")
public class EstabelecimentoController {

    private final EstabelecimentoService estabelecimentoService;

    @Operation(summary = "Buscar perfil do estabelecimento do usuário autenticado")
    @GetMapping("/me")
    public ResponseEntity<EstabelecimentoResponse> buscarMeuEstabelecimento(Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(estabelecimentoService.buscarPorEmailUsuario(principal.getName()));
    }

    @Operation(summary = "Buscar estabelecimento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estabelecimentoService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar perfil do estabelecimento (Etapa 2 do cadastro)")
    @PutMapping("/{id}")
    public ResponseEntity<EstabelecimentoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody EstabelecimentoAtualizacaoRequest request) {
        return ResponseEntity.ok(estabelecimentoService.atualizar(id, request));
    }

    @Operation(summary = "Listar todos os estabelecimentos ativos")
    @GetMapping
    public ResponseEntity<List<EstabelecimentoResponse>> listar() {
        return ResponseEntity.ok(estabelecimentoService.listarTodos());
    }
}
