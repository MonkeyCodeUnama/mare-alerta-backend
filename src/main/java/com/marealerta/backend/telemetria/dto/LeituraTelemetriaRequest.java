package com.marealerta.backend.telemetria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.marealerta.backend.telemetria.model.TipoRegistroTelemetria;

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
public class LeituraTelemetriaRequest {

    @NotBlank(message = "O código da estação é obrigatório")
    private String estacaoCodigo;

    private String estacaoNome;

    @NotNull(message = "O tipo de registro é obrigatório")
    private TipoRegistroTelemetria tipoRegistro;

    private BigDecimal nivelMare;

    private BigDecimal precipitacao;

    private BigDecimal temperatura;

    private BigDecimal ventoVelocidade;

    private BigDecimal ventoDirecao;

    @NotNull(message = "A data e hora da leitura são obrigatórias")
    private LocalDateTime dataHora;
}
