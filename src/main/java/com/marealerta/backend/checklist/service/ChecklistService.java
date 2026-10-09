package com.marealerta.backend.checklist.service;


import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.ItemChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.repository.ItemChecklistRepository;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final ItemChecklistRepository itemChecklistRepository;

    public ItemChecklistResponse criar(ItemChecklistRequest itemChecklistRequest){
        Estabelecimento estab = estabelecimentoRepository.findById(itemChecklistRequest.getEstabelecimentoId())
                .orElseThrow(() -> new RuntimeException("estabelecimento nao encontrado"));

        ItemChecklist itemChecklist = ItemChecklist.builder()
                .estabelecimento(estab)
                .titulo(itemChecklistRequest.getTitulo())
                .descricao(itemChecklistRequest.getDescricao())
                .categoria(itemChecklistRequest.getCategoria())
                .status(itemChecklistRequest.getStatus())
                .prioridade(itemChecklistRequest.getPrioridade())
                .ordem(itemChecklistRequest.getOrdem())
                .build();

        itemChecklist = itemChecklistRepository.save(itemChecklist);

        return ItemChecklistResponse.fromEntity(itemChecklist);

    }

    public List<ItemChecklistResponse> listar(Long estabelecimentoId, StatusChecklist statusChecklist){
        List<ItemChecklist> itens;

        if(statusChecklist != null){
             itens = itemChecklistRepository.findByEstabelecimentoIdAndStatusOrderByOrdemAsc(estabelecimentoId, statusChecklist);
        }else {
            itens = itemChecklistRepository.findByEstabelecimentoIdOrderByOrdemAsc(estabelecimentoId);
        }
        return itens.stream()
                .map(ItemChecklistResponse::fromEntity)
                .collect(Collectors.toList());

    }

    public void deletar(Long id){
        if(!itemChecklistRepository.existsById(id)){
            throw new RuntimeException("Item de checklist nao encontrado");
        }

        itemChecklistRepository.deleteById(id);
    }

    public ItemChecklistResponse buscarPorId(Long id){

        ItemChecklist itemChecklist = itemChecklistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de checklist nao encontrado"));

        return ItemChecklistResponse.fromEntity(itemChecklist);

    }

    public ItemChecklistResponse atualizar(Long id, ItemChecklistRequest itemChecklistRequest){
       ItemChecklist itemExistente = itemChecklistRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("item de checklist nao encontrado"));

        itemExistente.setTitulo(itemChecklistRequest.getTitulo());
        itemExistente.setDescricao(itemChecklistRequest.getDescricao());
        itemExistente.setCategoria(itemChecklistRequest.getCategoria());
        itemExistente.setStatus(itemChecklistRequest.getStatus());
        itemExistente.setPrioridade(itemChecklistRequest.getPrioridade());
        itemExistente.setOrdem(itemChecklistRequest.getOrdem());

        ItemChecklist itemAtualizado = itemChecklistRepository.save(itemExistente);

        return ItemChecklistResponse.fromEntity(itemAtualizado);
    }

}
