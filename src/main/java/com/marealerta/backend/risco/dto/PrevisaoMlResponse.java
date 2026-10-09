package com.marealerta.backend.risco.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrevisaoMlResponse {

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("issued_at")
    private Instant issuedAt;

    @JsonProperty("target_time")
<<<<<<< HEAD
    @JsonAlias("window_end")
=======
    @JsonAlias({"window_end", "target_time"})
>>>>>>> abdcf04 (feat(risco): alinha contrato rain-hourly-v1 da IA, adiciona truncamento UTC e fallback heuristico)
    private Instant targetTime;

    @JsonProperty("horizon_hours")
    private Integer horizonHours;

    @JsonProperty("rain_probability")
    private Double rainProbability;

    @JsonProperty("rain_expected")
    private Boolean rainExpected;

    @JsonProperty("expected_precipitation_mm")
    private BigDecimal expectedPrecipitationMm;

    @JsonProperty("quality_status")
    private StatusQualidadeMl qualityStatus;
}
