package com.marealerta.backend.risco.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenMeteoApiResponse(
        double latitude,
        double longitude,
        @JsonProperty("current_weather") CurrentWeather currentWeather
) {
    public record CurrentWeather(
            double temperature,
            double windspeed,
            int winddirection,
            int weathercode,
            String time
    ) {}
}