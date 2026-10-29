package com.marealerta.backend.itens.service;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.itens.dto.ItemCriticoRequest;
import com.marealerta.backend.itens.dto.ItemCriticoResponse;
import com.marealerta.backend.itens.model.ItemCritico;
import com.marealerta.backend.itens.repository.ItensCriticosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemCriticoService {

    private final ItensCriticosRepository itensCriticosRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    @Transactional
    public ItemCriticoResponse criar(ItemCriticoRequest itemCriticoRequest) {
        if (itemCriticoRequest == null || itemCriticoRequest.getEstabelecimentoId() == null) {
            throw new IllegalArgumentException("O ID do estabelecimento é obrigatório");
        }

        Estabelecimento estab = estabelecimentoRepository.findById(itemCriticoRequest.getEstabelecimentoId())
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + itemCriticoRequest.getEstabelecimentoId()));

        ItemCritico itemCritico = ItemCritico.builder()
                .nome(itemCriticoRequest.getNome() != null ? itemCriticoRequest.getNome().trim() : "")
                .descricao(itemCriticoRequest.getDescricao() != null ? itemCriticoRequest.getDescricao().trim() : null)
                .estabelecimento(estab)
                .build();

        itemCritico = itensCriticosRepository.save(itemCritico);

        return ItemCriticoResponse.fromEntity(itemCritico);
    }

    @Transactional(readOnly = true)
    public List<ItemCriticoResponse> listar(Long estabelecimentoId) {
        if (estabelecimentoId == null) {
            return List.of();
        }

        List<ItemCritico> itens = itensCriticosRepository.findByEstabelecimentoId(estabelecimentoId);
        if (itens == null) {
            return List.of();
        }

        return itens.stream()
                .map(ItemCriticoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemCriticoResponse buscarPorId(Long id) {
        ItemCritico itemCritico = itensCriticosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item crítico não encontrado com ID: " + id));

        return ItemCriticoResponse.fromEntity(itemCritico);
    }

    @Transactional
    public ItemCriticoResponse atualizar(Long id, ItemCriticoRequest itemCriticoRequest) {
        ItemCritico itemCritico = itensCriticosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item crítico não encontrado com ID: " + id));

        if (itemCriticoRequest.getNome() != null && !itemCriticoRequest.getNome().isBlank()) {
            itemCritico.setNome(itemCriticoRequest.getNome().trim());
        }
        if (itemCriticoRequest.getDescricao() != null) {
            itemCritico.setDescricao(itemCriticoRequest.getDescricao().trim());
        }

        ItemCritico itemCriticoAtualizado = itensCriticosRepository.save(itemCritico);

        return ItemCriticoResponse.fromEntity(itemCriticoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!itensCriticosRepository.existsById(id)) {
            throw new IllegalArgumentException("Item crítico não encontrado com ID: " + id);
        }

        itensCriticosRepository.deleteById(id);
    }
}
