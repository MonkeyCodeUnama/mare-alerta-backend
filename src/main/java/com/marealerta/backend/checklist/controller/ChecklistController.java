package com.marealerta.backend.checklist.controller;

import com.marealerta.backend.checklist.dto.ChecklistResumoDTO;
import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.service.ChecklistService;
import com.marealerta.backend.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklist", description = "Endpoints de gerenciamento dos checklists de mitigação do estabelecimento")
public class ChecklistController {

    private final ChecklistService checklistService;

    @Operation(summary = "Criar item de checklist para o estabelecimento")
    @PostMapping({"", "/registrar"})
    public ResponseEntity<ItemChecklistResponse> registrar(@Valid @RequestBody ItemChecklistRequest itemChecklistRequest) {
        ItemChecklistResponse itemChecklistResponse = checklistService.criar(itemChecklistRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemChecklistResponse);
    }

    @Operation(summary = "Listar itens de checklist do estabelecimento")
    @GetMapping({"", "/listar"})
    public ResponseEntity<List<ItemChecklistResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) Long estabelecimentoId,
            @RequestParam(required = false) Long estabelcimentoId,
            @RequestParam(required = false) StatusChecklist statusChecklist) {
        Long idFinal = estabelecimentoId != null ? estabelecimentoId : estabelcimentoId;
        if (idFinal == null && usuario != null && usuario.getEstabelecimento() != null) {
            idFinal = usuario.getEstabelecimento().getId();
        }
        List<ItemChecklistResponse> itens = checklistService.listar(idFinal, statusChecklist);
        return ResponseEntity.ok(itens);
    }

    @Operation(summary = "Obter resumo estatístico de progresso do checklist")
    @GetMapping("/resumo")
    public ResponseEntity<ChecklistResumoDTO> obterResumo(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) Long estabelecimentoId) {
        Long idFinal = estabelecimentoId;
        if (idFinal == null && usuario != null && usuario.getEstabelecimento() != null) {
            idFinal = usuario.getEstabelecimento().getId();
        }
        ChecklistResumoDTO resumo = checklistService.obterResumo(idFinal);
        return ResponseEntity.ok(resumo);
    }

    @Operation(summary = "Buscar item de checklist por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ItemChecklistResponse> buscarPorId(@PathVariable Long id) {
        ItemChecklistResponse itemChecklistResponse = checklistService.buscarPorId(id);
        return ResponseEntity.ok(itemChecklistResponse);
    }

    @Operation(summary = "Atualizar item de checklist")
    @PutMapping("/{id}")
    public ResponseEntity<ItemChecklistResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ItemChecklistRequest itemChecklistRequest) {
        ItemChecklistResponse itemChecklistResponse = checklistService.atualizar(id, itemChecklistRequest);
        return ResponseEntity.ok(itemChecklistResponse);
    }

    @Operation(summary = "Alternar status do item de checklist (concluído/pendente)")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ItemChecklistResponse> alternarStatus(@PathVariable Long id) {
        ItemChecklistResponse response = checklistService.alternarStatus(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Marcar item de checklist como concluído")
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<ItemChecklistResponse> marcarComoConcluido(@PathVariable Long id) {
        ItemChecklistResponse response = checklistService.marcarComoConcluido(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Gerar itens de checklist padrão de acordo com o segmento do estabelecimento")
    @PostMapping("/gerar-padrao")
    public ResponseEntity<List<ItemChecklistResponse>> gerarPadrao(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) Long estabelecimentoId) {
        Long idFinal = estabelecimentoId;
        if (idFinal == null && usuario != null && usuario.getEstabelecimento() != null) {
            idFinal = usuario.getEstabelecimento().getId();
        }
        List<ItemChecklistResponse> itens = checklistService.gerarItensPadrao(idFinal);
        return ResponseEntity.status(HttpStatus.CREATED).body(itens);
    }

    @Operation(summary = "Deletar item de checklist")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        checklistService.deletar(id);
    }
}
