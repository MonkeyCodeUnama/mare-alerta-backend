package com.marealerta.backend.estabelecimento.dto;

import com.marealerta.backend.estabelecimento.model.TipoNegocio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private TipoNegocio tipoNegocio;

    @NotBlank
    private String endereco;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

}
