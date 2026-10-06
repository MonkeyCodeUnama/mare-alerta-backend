package com.marealerta.backend.risco.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class PrevisaoMlRequest {

    private String localidade;
    private LocalDateTime instanteReferencia;
    private Integer horizonteHoras;
    private BigDecimal temperatura2m;
    private BigDecimal umidadeRelativa2m;
    private BigDecimal pressaoSuperficie;
    private BigDecimal coberturaNuvens;
    private BigDecimal velocidadeVento10m;
    private BigDecimal direcaoVento10m;
    private BigDecimal precipitacaoObservada1h;
    private BigDecimal cape;
}

