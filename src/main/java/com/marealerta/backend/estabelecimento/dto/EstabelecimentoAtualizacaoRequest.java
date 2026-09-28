package com.marealerta.backend.estabelecimento.dto;

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

    private String nome;
    private String documento;
    private String responsavel;
    private Bairro bairro;
    private String logradouro;
    private String numero;
    private String complemento;
    private String cep;
    private String municipio;
    private String uf;
    private String telefone;
    private TipoNegocio tipoNegocio;
}
