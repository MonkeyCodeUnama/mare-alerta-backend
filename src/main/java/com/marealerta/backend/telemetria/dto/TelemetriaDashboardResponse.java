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
public class TelemetriaDashboardResponse {

    private String estacaoCodigo;
    private String estacaoNome;
    private BigDecimal nivelAtual;
    private String tendencia;
    private LocalDateTime preamarHorario;
    private BigDecimal chuvaAtual;
    private BigDecimal temperatura;
    private BigDecimal ventoVelocidade;
    private BigDecimal ventoDirecao;
    private String nivelRisco;
    private LocalDateTime dataHora;
    private String unidadeNivel;
    private String unidadeChuva;
    private String unidadeTemperatura;
    private String unidadeVento;
}
