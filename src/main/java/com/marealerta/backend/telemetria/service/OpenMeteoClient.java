package com.marealerta.backend.telemetria.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OpenMeteoClient {

    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
    private static final double LATITUDE_BELEM = -1.4558;
    private static final double LONGITUDE_BELEM = -48.5044;

    private final RestTemplate restTemplate;

    public OpenMeteoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OpenMeteoResponse buscarDadosAtuaisEPrevisao() {
        String url = UriComponentsBuilder.fromUriString(OPEN_METEO_URL)
                .queryParam("latitude", LATITUDE_BELEM)
                .queryParam("longitude", LONGITUDE_BELEM)
                .queryParam("current", "precipitation,temperature_2m,wind_speed_10m,wind_direction_10m")
                .queryParam("hourly", "precipitation,temperature_2m,wind_speed_10m,wind_direction_10m")
                .queryParam("timezone", "America/Belem")
                .toUriString();

        return restTemplate.getForObject(url, OpenMeteoResponse.class);
    }

    public record OpenMeteoResponse(
            @JsonProperty("current") CurrentUnits current,
            @JsonProperty("hourly") HourlyData hourly
    ) {}

    public record CurrentUnits(
            String time,
            @JsonProperty("precipitation") BigDecimal precipitacao,
            @JsonProperty("temperature_2m") BigDecimal temperatura,
            @JsonProperty("wind_speed_10m") BigDecimal ventoVelocidade,
            @JsonProperty("wind_direction_10m") BigDecimal ventoDirecao
    ) {}

    public record HourlyData(
            List<String> time,
            @JsonProperty("precipitation") List<BigDecimal> precipitacao,
            @JsonProperty("temperature_2m") List<BigDecimal> temperatura,
            @JsonProperty("wind_speed_10m") List<BigDecimal> ventoVelocidade,
            @JsonProperty("wind_direction_10m") List<BigDecimal> ventoDirecao
    ) {}
}