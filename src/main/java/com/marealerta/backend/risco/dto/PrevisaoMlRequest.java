package com.marealerta.backend.risco.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public record PrevisaoMlRequest(
        @JsonProperty("schema_version") String schemaVersion,
        BigDecimal latitude,
        BigDecimal longitude,
        @JsonProperty("feature_time") String featureTime,
        @JsonProperty("horizon_hours") Integer horizonHours,
        Map<String, BigDecimal> features,
        Map<String, String> units
) {}