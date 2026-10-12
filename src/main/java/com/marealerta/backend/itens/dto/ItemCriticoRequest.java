package com.marealerta.backend.itens.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemCriticoRequest {

    private String nome;

    private String descricao;

    @NotNull(message = "O ID do estabelecimento é obrigatório")
    private Long estabelecimentoId;


}
