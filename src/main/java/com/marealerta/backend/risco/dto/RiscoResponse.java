package com.marealerta.backend.risco.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.marealerta.backend.alerta.model.NivelRisco;

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
public class RiscoResponse {

    private String bairro;
    private NivelRisco nivelRisco;
    private Double probabilidadeChuva;
    private BigDecimal nivelMarePrevistoMetros;
    private String descricaoCenario;
    private LocalDateTime horarioPicoPrevisto;
    private Boolean alertaEmitido;
}

