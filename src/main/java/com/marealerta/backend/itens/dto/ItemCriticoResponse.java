package com.marealerta.backend.itens.dto;

import com.marealerta.backend.itens.model.ItemCritico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCriticoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private Long estabelecimentoId;

    public String getName() { return nome; }
    public String getDescription() { return descricao; }
    public Long getEstablishmentId() { return estabelecimentoId; }

    public static ItemCriticoResponse fromEntity(ItemCritico item) {
        if (item == null) return null;
        return ItemCriticoResponse.builder()
                .id(item.getId())
                .nome(item.getNome())
                .descricao(item.getDescricao())
                .estabelecimentoId(item.getEstabelecimento() != null ? item.getEstabelecimento().getId() : null)
                .build();
    }
}
