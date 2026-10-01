package com.marealerta.backend.checklist.dto;

import com.marealerta.backend.checklist.model.PrioridadeChecklist;
import com.marealerta.backend.checklist.model.StatusChecklist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemChecklistRequest {

    @NotNull(message = "O ID do estabelecimento é obrigatório")
    private Long estabelecimentoId;

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    private String descricao;

    private String categoria;

    @Builder.Default
    private StatusChecklist status = StatusChecklist.PENDENTE;

    @Builder.Default
    private PrioridadeChecklist prioridade = PrioridadeChecklist.MEDIA;

    @Builder.Default
    private Integer ordem = 0;
}
