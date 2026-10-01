package com.marealerta.backend.telemetria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.marealerta.backend.telemetria.model.LeituraTelemetria;
import com.marealerta.backend.telemetria.model.TipoRegistroTelemetria;

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
public class LeituraTelemetriaResponse {

    private Long id;
    private String estacaoCodigo;
    private String estacaoNome;
    private TipoRegistroTelemetria tipoRegistro;
    private BigDecimal nivelMare;
    private BigDecimal precipitacao;
    private LocalDateTime dataHora;
    private LocalDateTime createdAt;

    public static LeituraTelemetriaResponse fromEntity(LeituraTelemetria l) {
        if (l == null) return null;
        return LeituraTelemetriaResponse.builder()
                .id(l.getId())
                .estacaoCodigo(l.getEstacaoCodigo())
                .estacaoNome(l.getEstacaoNome())
                .tipoRegistro(l.getTipoRegistro())
                .nivelMare(l.getNivelMare())
                .precipitacao(l.getPrecipitacao())
                .dataHora(l.getDataHora())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
