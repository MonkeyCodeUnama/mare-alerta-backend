package com.marealerta.backend.checklist.service;

import com.marealerta.backend.checklist.dto.ItemChecklistRequest;
import com.marealerta.backend.checklist.dto.ItemChecklistResponse;
import com.marealerta.backend.checklist.model.ItemChecklist;
import com.marealerta.backend.checklist.model.PrioridadeChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;
import com.marealerta.backend.checklist.repository.ItemChecklistRepository;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final ItemChecklistRepository itemChecklistRepository;

    @Transactional
    public ItemChecklistResponse criar(ItemChecklistRequest itemChecklistRequest) {
        if (itemChecklistRequest.getEstabelecimentoId() == null) {
            throw new IllegalArgumentException("O ID do estabelecimento é obrigatório");
        }

        Estabelecimento estab = estabelecimentoRepository.findById(itemChecklistRequest.getEstabelecimentoId())
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + itemChecklistRequest.getEstabelecimentoId()));

        ItemChecklist itemChecklist = ItemChecklist.builder()
                .estabelecimento(estab)
                .titulo(itemChecklistRequest.getTitulo())
                .descricao(itemChecklistRequest.getDescricao())
                .categoria(itemChecklistRequest.getCategoria())
                .status(itemChecklistRequest.getStatus() != null ? itemChecklistRequest.getStatus() : StatusChecklist.PENDENTE)
                .prioridade(itemChecklistRequest.getPrioridade() != null ? itemChecklistRequest.getPrioridade() : PrioridadeChecklist.MEDIA)
                .ordem(itemChecklistRequest.getOrdem() != null ? itemChecklistRequest.getOrdem() : 0)
                .build();

        itemChecklist = itemChecklistRepository.save(itemChecklist);

        return ItemChecklistResponse.fromEntity(itemChecklist);
    }

    @Transactional(readOnly = true)
    public List<ItemChecklistResponse> listar(Long estabelecimentoId, StatusChecklist statusChecklist) {
        if (estabelecimentoId == null) {
            return List.of();
        }

        List<ItemChecklist> itens;

        if (statusChecklist != null) {
            itens = itemChecklistRepository.findByEstabelecimentoIdAndStatusOrderByOrdemAsc(estabelecimentoId, statusChecklist);
        } else {
            itens = itemChecklistRepository.findByEstabelecimentoIdOrderByOrdemAsc(estabelecimentoId);
        }

        return itens.stream()
                .map(ItemChecklistResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemChecklistResponse buscarPorId(Long id) {
        ItemChecklist item = itemChecklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item de checklist não encontrado com ID: " + id));
        return ItemChecklistResponse.fromEntity(item);
    }

    @Transactional
    public ItemChecklistResponse atualizar(Long id, ItemChecklistRequest request) {
        ItemChecklist item = itemChecklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item de checklist não encontrado com ID: " + id));

        if (request.getTitulo() != null && !request.getTitulo().isBlank()) {
            item.setTitulo(request.getTitulo());
        }
        if (request.getDescricao() != null) {
            item.setDescricao(request.getDescricao());
        }
        if (request.getCategoria() != null) {
            item.setCategoria(request.getCategoria());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (request.getPrioridade() != null) {
            item.setPrioridade(request.getPrioridade());
        }
        if (request.getOrdem() != null) {
            item.setOrdem(request.getOrdem());
        }

        item = itemChecklistRepository.save(item);

        return ItemChecklistResponse.fromEntity(item);
    }

    @Transactional
    public void deletar(Long id) {
        if (!itemChecklistRepository.existsById(id)) {
            throw new IllegalArgumentException("Item de checklist não encontrado com ID: " + id);
        }
        itemChecklistRepository.deleteById(id);
    }
}
