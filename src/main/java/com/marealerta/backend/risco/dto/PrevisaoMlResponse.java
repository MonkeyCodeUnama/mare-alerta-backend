package com.marealerta.backend.risco.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.marealerta.backend.risco.model.StatusQualidadeMl;

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
public class PrevisaoMlResponse {

    private String location;
    private Instant issuedAt;
    private Instant targetTime;
    private Integer horizonHours;
    private Double rainProbability;
    private Boolean rainExpected;
    private BigDecimal expectedPrecipitationMm;
    private String modelVersion;
    private String dataVersion;
    private StatusQualidadeMl qualityStatus;
}
