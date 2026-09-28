package com.marealerta.backend.estabelecimento.dto;

import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;

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
    private String nome;

    @NotBlank(message = "O documento/CNPJ é obrigatório")
    private String documento;

    @NotNull(message = "O bairro é obrigatório")
    private Bairro bairro;

    // Campos da Etapa 2 (opcionais no cadastro inicial, preenchidos na tela /estabelecimento)
    private String responsavel;

    private String logradouro;

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
