package com.marealerta.backend.estabelecimento.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;

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
public class EstabelecimentoAtualizacaoRequest {

    @JsonAlias({"name"})
    private String nome;

    @JsonAlias({"cnpj"})
    private String documento;

    @JsonAlias({"owner"})
    private String responsavel;

    @JsonAlias({"neighborhood"})
    private Bairro bairro;

    @JsonAlias({"street"})
    private String logradouro;

    @JsonAlias({"number"})
    private String numero;

    private String complemento;
    private String cep;
    private String municipio;
    private String uf;
    private String telefone;

    @JsonAlias({"type"})
    private TipoNegocio tipoNegocio;

    @JsonAlias({"days"})
    private List<String> dias;

    @JsonAlias({"opening"})
    private String horarioAbertura;

    @JsonAlias({"closing"})
    private String horarioFechamento;

    @JsonAlias({"criticalItems"})
    private List<String> itensCriticos;
}
