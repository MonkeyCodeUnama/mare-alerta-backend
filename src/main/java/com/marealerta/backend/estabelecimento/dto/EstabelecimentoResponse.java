package com.marealerta.backend.estabelecimento.dto;

import java.util.List;

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

    private List<String> dias;
    private String horarioAbertura;
    private String horarioFechamento;
    private List<String> itensCriticos;

    public String getName() { return nome; }
    public String getOwner() { return responsavel; }
    public String getCnpj() { return documento; }
    public String getNeighborhood() { return bairroDescricao != null ? bairroDescricao : (bairro != null ? bairro.getDescricao() : null); }
    public String getStreet() { return logradouro; }
    public String getNumber() { return numero; }
    public String getType() { return tipoNegocio != null ? tipoNegocio.name() : null; }
    public List<String> getDays() { return dias; }
    public String getOpening() { return horarioAbertura; }
    public String getClosing() { return horarioFechamento; }
    public List<String> getCriticalItems() { return itensCriticos; }

    public static EstabelecimentoResponse fromEntity(Estabelecimento e) {
        return fromEntity(e, null, null, null, null);
    }

    public static EstabelecimentoResponse fromEntity(
            Estabelecimento e,
            List<String> dias,
            String horarioAbertura,
            String horarioFechamento,
            List<String> itensCriticos
    ) {
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
                .dias(dias)
                .horarioAbertura(horarioAbertura)
                .horarioFechamento(horarioFechamento)
                .itensCriticos(itensCriticos)
                .build();
    }
}
