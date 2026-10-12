package com.marealerta.backend.telemetria.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenMeteoClient {

    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
    private static final double LATITUDE_BELEM = -1.4558;
    private static final double LONGITUDE_BELEM = -48.5044;

    private final RestTemplate restTemplate;

    public OpenMeteoResponse buscarDadosAtuaisEPrevisao() {
        String url = UriComponentsBuilder.fromUriString(OPEN_METEO_URL)
                .queryParam("latitude", LATITUDE_BELEM)
                .queryParam("longitude", LONGITUDE_BELEM)
                .queryParam("current", "precipitation,temperature_2m,relative_humidity_2m,dew_point_2m,surface_pressure,pressure_msl,cloud_cover,wind_speed_10m,wind_direction_10m,wind_gusts_10m")
                .queryParam("hourly", "precipitation,temperature_2m,wind_speed_10m,wind_direction_10m")
                .queryParam("timezone", "America/Belem")
                .toUriString();

        return restTemplate.getForObject(url, OpenMeteoResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenMeteoResponse(
            @JsonProperty("current") CurrentUnits current,
            @JsonProperty("hourly") HourlyData hourly
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentUnits(
            String time,
            @JsonProperty("precipitation") BigDecimal precipitacao,
            @JsonProperty("temperature_2m") BigDecimal temperatura,
            @JsonProperty("relative_humidity_2m") BigDecimal umidadeRelativa,
            @JsonProperty("dew_point_2m") BigDecimal pontoOrvalho,
            @JsonProperty("surface_pressure") BigDecimal pressaoSuperficie,
            @JsonProperty("pressure_msl") BigDecimal pressaoMsl,
            @JsonProperty("cloud_cover") BigDecimal coberturaNuvens,
            @JsonProperty("wind_speed_10m") BigDecimal ventoVelocidade,
            @JsonProperty("wind_direction_10m") BigDecimal ventoDirecao,
            @JsonProperty("wind_gusts_10m") BigDecimal rajadasVento
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HourlyData(
            List<String> time,
            @JsonProperty("precipitation") List<BigDecimal> precipitacao,
            @JsonProperty("temperature_2m") List<BigDecimal> temperatura,
            @JsonProperty("wind_speed_10m") List<BigDecimal> ventoVelocidade,
            @JsonProperty("wind_direction_10m") List<BigDecimal> ventoDirecao
    ) {}
}