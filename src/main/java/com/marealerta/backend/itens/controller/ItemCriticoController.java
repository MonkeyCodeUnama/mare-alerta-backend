package com.marealerta.backend.itens.controller;

import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.itens.dto.ItemCriticoRequest;
import com.marealerta.backend.itens.dto.ItemCriticoResponse;
import com.marealerta.backend.itens.service.ItemCriticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("itemcritico")
@RequiredArgsConstructor
@Tag(name = "ItemCritico", description = "Endpoints de gerenciamento dos itens criticos dos estabelecimento comercial")
public class ItemCriticoController {

    private final ItemCriticoService itemCriticoService;

    @Operation(summary = "criar um item critico para o estabelecimento")
    @PostMapping("/registrar")
    public ResponseEntity<ItemCriticoResponse> registrar(@RequestBody ItemCriticoRequest itemCriticoRequest){
        ItemCriticoResponse item = itemCriticoService.criar(itemCriticoRequest);
        return ResponseEntity.ok(item);
    }

    @Operation(summary = "listar itens criticos do estabelcimento")
    @GetMapping("/listar")
    public ResponseEntity<List<ItemCriticoResponse>> listar(@RequestParam("estabelecimentoId") Long estabelecimentoId){
        List<ItemCriticoResponse> itens = itemCriticoService.listar(estabelecimentoId);
        return ResponseEntity.ok(itens);
    }

    @Operation(summary = "buscar item critico por id")
    @GetMapping("/{id}")
    public ResponseEntity<ItemCriticoResponse> buscarPorId(@PathVariable Long id){
        ItemCriticoResponse item = itemCriticoService.buscarPorId(id);
        return ResponseEntity.ok(item);
    }

    @Operation(summary = "atualizar um item critico")
    @PutMapping("/{id}")
    public ResponseEntity<ItemCriticoResponse> atualizar(@PathVariable Long id, @RequestBody ItemCriticoRequest itemCriticoRequest){
        ItemCriticoResponse item = itemCriticoService.atualizar(id, itemCriticoRequest);
        return ResponseEntity.ok(item);
    }

    @Operation(summary = "deletar item")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id){
        itemCriticoService.deletar(id);
    }

    
}
