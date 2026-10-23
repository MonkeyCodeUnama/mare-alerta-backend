package com.marealerta.backend.itens.service;

import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.repository.EstabelecimentoRepository;
import com.marealerta.backend.itens.dto.ItemCriticoRequest;
import com.marealerta.backend.itens.dto.ItemCriticoResponse;
import com.marealerta.backend.itens.model.ItemCritico;
import com.marealerta.backend.itens.repository.ItensCriticosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemCriticoService {

    private final ItensCriticosRepository itensCriticosRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    public ItemCriticoResponse criar(ItemCriticoRequest itemCriticoRequest){
        Estabelecimento estab = estabelecimentoRepository.findById(itemCriticoRequest.getEstabelecimentoId())
                .orElseThrow(() -> new RuntimeException("estabelecimento nao encontrado"));

        ItemCritico itemCritico = ItemCritico.builder()
                .nome(itemCriticoRequest.getNome())
                .descricao(itemCriticoRequest.getDescricao())
                .estabelecimento(estab)
                .build();

        itemCritico = itensCriticosRepository.save(itemCritico);

        return ItemCriticoResponse.fromEntity(itemCritico);
    }

    public List<ItemCriticoResponse> listar(Long estabelecimentoId){
        List<ItemCritico> itens = itensCriticosRepository.findByEstabelecimentoId(estabelecimentoId);

        if(itens == null){
            throw new RuntimeException("nao ha itens criticos cadastrados");
        }

        return itens.stream()
                .map(ItemCriticoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ItemCriticoResponse buscarPorId(Long id){
        ItemCritico itemCritico = itensCriticosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("item critico nao encontrado"));

        return ItemCriticoResponse.fromEntity(itemCritico);
    }

    public ItemCriticoResponse atualizar(Long id, ItemCriticoRequest itemCriticoRequest){
        ItemCritico itemCritico = itensCriticosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("item critico nao encontrado"));

        itemCritico.setNome(itemCriticoRequest.getNome());
        itemCritico.setDescricao(itemCriticoRequest.getDescricao());

        ItemCritico itemCriticoAtualizado = itensCriticosRepository.save(itemCritico);

        return ItemCriticoResponse.fromEntity(itemCriticoAtualizado);
    }

    public void deletar(Long id){
        if(!itensCriticosRepository.existsById(id)){
            throw new RuntimeException("item critico nao encontrado");
        }

        itensCriticosRepository.deleteById(id);
    }
}
