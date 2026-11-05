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
    private String nivelFormatado;
    private String tendencia;
    private LocalDateTime preamarHorario;
    private String preamarHorarioFormatado;
    private BigDecimal chuvaAtual;
    private String chuvaFormatada;
    private BigDecimal temperatura;
    private String temperaturaFormatada;
    private BigDecimal ventoVelocidade;
    private String ventoFormatado;
    private BigDecimal ventoDirecao;
    private String nivelRisco;
    private LocalDateTime dataHora;
    private String unidadeNivel;
    private String unidadeChuva;
    private String unidadeTemperatura;
    private String unidadeVento;

    public BigDecimal getCurrentLevel() { return nivelAtual; }
    public BigDecimal getLevel() { return nivelAtual; }
    public BigDecimal getRain() { return chuvaAtual; }
    public BigDecimal getWind() { return ventoVelocidade; }
    public String getRiskLevel() { return nivelRisco; }
}
