package com.marealerta.backend.checklist.dto;

import java.time.LocalDateTime;

import com.marealerta.backend.checklist.model.ItemChecklist;
import com.marealerta.backend.checklist.model.PrioridadeChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;

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
public class ItemChecklistResponse {

    private Long id;
    private Long estabelecimentoId;
    private String titulo;
    private String descricao;
    private String categoria;
    private StatusChecklist status;
    private PrioridadeChecklist prioridade;
    private Integer ordem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemChecklistResponse fromEntity(ItemChecklist item) {
        if (item == null) return null;
        return ItemChecklistResponse.builder()
                .id(item.getId())
                .estabelecimentoId(item.getEstabelecimento() != null ? item.getEstabelecimento().getId() : null)
                .titulo(item.getTitulo())
                .descricao(item.getDescricao())
                .categoria(item.getCategoria())
                .status(item.getStatus())
                .prioridade(item.getPrioridade())
                .ordem(item.getOrdem())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
