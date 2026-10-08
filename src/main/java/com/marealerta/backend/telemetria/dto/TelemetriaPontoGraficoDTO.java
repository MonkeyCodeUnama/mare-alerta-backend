package com.marealerta.backend.telemetria.dto;

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
public class TelemetriaPontoGraficoDTO {

    private String time;
    private LocalDateTime dataHora;
    private BigDecimal level;
    private BigDecimal rain;
    private BigDecimal temperature;
    private BigDecimal wind;
    private String tipoRegistro;
}
