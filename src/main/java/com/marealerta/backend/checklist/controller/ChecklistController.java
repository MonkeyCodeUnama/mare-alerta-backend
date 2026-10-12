package com.marealerta.backend.checklist.controller;

import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.ItemChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("checklists")
@RequiredArgsConstructor
@Tag(name = "Checklista", description = "Endpoints de gerenciamento dos checklists dos estabelecimento comercial")
public class ChecklistController {

    private final ChecklistService checklistService;

    @Operation(summary = "criar um checklist para o estabelecimento")
    @PostMapping("/registrar")
    public ResponseEntity<ItemChecklistResponse> registrar(@RequestBody ItemChecklistRequest itemChecklistRequest){
        ItemChecklistResponse itemChecklistResponse = checklistService.criar(itemChecklistRequest);
        return ResponseEntity.ok(itemChecklistResponse);
    }

    @Operation(summary = "listar itens checklists do estabelcimento")
    @GetMapping("/listar")
    public ResponseEntity<List<ItemChecklistResponse>> listar(@RequestParam("estabelecimentoId") Long estabelecimentoId, @RequestParam(required = false)StatusChecklist statusChecklist){
        List<ItemChecklistResponse> itens = checklistService.listar(estabelecimentoId, statusChecklist);
        return ResponseEntity.ok(itens);
    }

    @Operation(summary = "buscar item checklist por id")
    @GetMapping("/{id}")
    public ResponseEntity<ItemChecklistResponse> buscarPorId(@PathVariable Long id){
        ItemChecklistResponse itemChecklistResponse = checklistService.buscarPorId(id);
        return ResponseEntity.ok(itemChecklistResponse);
    }

    @Operation(summary = "atualizar um checklist")
    @PutMapping("/{id}")
    public ResponseEntity<ItemChecklistResponse> atualizar(@PathVariable Long id, @RequestBody ItemChecklistRequest itemChecklistRequest){
        ItemChecklistResponse itemChecklistResponse = checklistService.atualizar(id, itemChecklistRequest);
        return ResponseEntity.ok(itemChecklistResponse);
    }

    @Operation(summary = "deletar chekclist")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id){
        checklistService.deletar(id);
    }
}
