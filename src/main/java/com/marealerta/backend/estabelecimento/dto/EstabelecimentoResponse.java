package com.marealerta.backend.estabelecimento.dto;

import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EstabelecimentoResponse {

    private Long id;
    private String nome;
    private String documento;
    private TipoNegocio tipoNegocio;
    private String endereco;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<HorarioResponse> horarioResponses;
}
