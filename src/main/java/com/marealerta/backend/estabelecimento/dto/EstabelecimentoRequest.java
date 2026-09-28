package com.marealerta.backend.estabelecimento.dto;

import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class EstabelecimentoRequest {

    @NotBlank(message = "O nome do estabelecimento é obrigatório")
    @JsonAlias({"name"})
    private String nome;

    @NotBlank(message = "O documento/CNPJ é obrigatório")
    @JsonAlias({"cnpj"})
    private String documento;

    @NotNull(message = "O bairro é obrigatório")
    @JsonAlias({"neighborhood"})
    private Bairro bairro;

    // Campos da Etapa 2 (opcionais no cadastro inicial, preenchidos na tela /estabelecimento)
    @JsonAlias({"owner"})
    private String responsavel;

    @JsonAlias({"street"})
    private String logradouro;

    @JsonAlias({"number"})
    private String numero;

    private String complemento;

    private String cep;

    @Builder.Default
    private String municipio = "Belém";

    @Builder.Default
    private String uf = "PA";

    private String telefone;

    @Builder.Default
    private TipoNegocio tipoNegocio = TipoNegocio.OUTROS;
}
