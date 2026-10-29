package com.marealerta.backend.itens.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
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
public class ItemCriticoRequest {

    @NotBlank(message = "O nome do item é obrigatório")
    @JsonAlias({"name", "itemNome"})
    private String nome;

    @JsonAlias({"description"})
    private String descricao;

    @JsonAlias({"establishmentId", "estabelcimentoId"})
    private Long estabelecimentoId;
}
