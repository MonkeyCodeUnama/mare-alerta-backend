package com.marealerta.backend.checklist.controller;

import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestParam(required = false) Long estabelecimentoId,
            @RequestParam(required = false) Long estabelcimentoId,
            @RequestParam(required = false) StatusChecklist statusChecklist) {
        Long idFinal = estabelecimentoId != null ? estabelecimentoId : estabelcimentoId;
        List<ItemChecklistResponse> itens = checklistService.listar(idFinal, statusChecklist);
        return ResponseEntity.ok(itens);
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

    @Operation(summary = "Deletar item de checklist")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        checklistService.deletar(id);
    }
}
