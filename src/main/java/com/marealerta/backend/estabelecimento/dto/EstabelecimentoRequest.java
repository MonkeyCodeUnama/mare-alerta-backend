package com.marealerta.backend.estabelecimento.dto;

import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstabelecimentoRequest {

    @NotBlank
    private String nome;

    @NotBlank
    private String documento;

    @NotBlank
    private TipoNegocio tipoNegocio;

    @NotBlank
    private String endereco;

    @NotBlank
    private BigDecimal latitude;

    @NotBlank
    private BigDecimal longitude;

}
