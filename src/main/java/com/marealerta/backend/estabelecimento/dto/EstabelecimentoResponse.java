package com.marealerta.backend.estabelecimento.dto;

import com.marealerta.backend.estabelecimento.model.Bairro;
import com.marealerta.backend.estabelecimento.model.Estabelecimento;
import com.marealerta.backend.estabelecimento.model.TipoNegocio;

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
public class EstabelecimentoResponse {

    private Long id;
    private String nome;
    private String documento;
    private TipoNegocio tipoNegocio;
    private String responsavel;
    private Bairro bairro;
    private String bairroDescricao;
    private String logradouro;
    private String numero;
    private String complemento;
    private String cep;
    private String municipio;
    private String uf;
    private String telefone;
    private Boolean ativo;

    public static EstabelecimentoResponse fromEntity(Estabelecimento e) {
        if (e == null) return null;
        return EstabelecimentoResponse.builder()
                .id(e.getId())
                .nome(e.getNome())
                .documento(e.getDocumento())
                .tipoNegocio(e.getTipoNegocio())
                .responsavel(e.getResponsavel())
                .bairro(e.getBairro())
                .bairroDescricao(e.getBairro() != null ? e.getBairro().getDescricao() : null)
                .logradouro(e.getLogradouro())
                .numero(e.getNumero())
                .complemento(e.getComplemento())
                .cep(e.getCep())
                .municipio(e.getMunicipio())
                .uf(e.getUf())
                .telefone(e.getTelefone())
                .ativo(e.getAtivo())
                .build();
    }
}
