package com.marealerta.backend.telemetria.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final String OPEN_METEO_MARINE_URL = "https://marine-api.open-meteo.com/v1/marine";
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

    public OpenMeteoMarineResponse buscarDadosMarinhos(Double latitude, Double longitude) {
        double lat = latitude != null ? latitude : LATITUDE_BELEM;
        double lon = longitude != null ? longitude : LONGITUDE_BELEM;

        String url = UriComponentsBuilder.fromUriString(OPEN_METEO_MARINE_URL)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("current", "sea_level_height_msl")
                .queryParam("hourly", "sea_level_height_msl")
                .queryParam("timezone", "America/Belem")
                .queryParam("past_days", 1)
                .queryParam("forecast_days", 2)
                .queryParam("cell_selection", "sea")
                .toUriString();

        return restTemplate.getForObject(url, OpenMeteoMarineResponse.class);
    }

    public GfsFeaturesSnapshot buscarFeaturesGfsAtuais(Double latitude, Double longitude) {
        double lat = latitude != null ? latitude : LATITUDE_BELEM;
        double lon = longitude != null ? longitude : LONGITUDE_BELEM;

        String url = UriComponentsBuilder.fromUriString(OPEN_METEO_URL)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("models", "gfs_global")
                .queryParam("timezone", "UTC")
                .queryParam("past_hours", 1)
                .queryParam("forecast_hours", 1)
                .queryParam("hourly", "temperature_2m,relative_humidity_2m,dew_point_2m,pressure_msl,surface_pressure,cloud_cover,cloud_cover_low,cloud_cover_mid,cloud_cover_high,wind_speed_10m,wind_direction_10m,wind_gusts_10m,cape,relative_humidity_850hPa,relative_humidity_700hPa,relative_humidity_500hPa,temperature_850hPa,temperature_700hPa,geopotential_height_500hPa,precipitation")
                .toUriString();

        OpenMeteoGfsResponse response = restTemplate.getForObject(url, OpenMeteoGfsResponse.class);
        if (response == null || response.hourly() == null || response.hourly().time() == null || response.hourly().time().isEmpty()) {
            throw new RuntimeException("Falha ao obter features meteorologicas do modelo GFS.");
        }

        var hourly = response.hourly();
        String currentUtcHourPrefix = OffsetDateTime.now(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.HOURS)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00"));

        int targetIndex = -1;
        for (int i = 0; i < hourly.time().size(); i++) {
            if (hourly.time().get(i).startsWith(currentUtcHourPrefix)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            targetIndex = hourly.time().size() - 1;
        }

        String matchedTime = hourly.time().get(targetIndex);
        String featureTime = matchedTime.endsWith("Z") ? matchedTime : matchedTime + ":00Z";

        Map<String, BigDecimal> features = new LinkedHashMap<>();
        features.put("temperature_2m", getAtIndex(hourly.temperature2m(), targetIndex));
        features.put("relative_humidity_2m", getAtIndex(hourly.relativeHumidity2m(), targetIndex));
        features.put("dew_point_2m", getAtIndex(hourly.dewPoint2m(), targetIndex));
        features.put("pressure_msl", getAtIndex(hourly.pressureMsl(), targetIndex));
        features.put("surface_pressure", getAtIndex(hourly.surfacePressure(), targetIndex));
        features.put("cloud_cover", getAtIndex(hourly.cloudCover(), targetIndex));
        features.put("cloud_cover_low", getAtIndex(hourly.cloudCoverLow(), targetIndex));
        features.put("cloud_cover_mid", getAtIndex(hourly.cloudCoverMid(), targetIndex));
        features.put("cloud_cover_high", getAtIndex(hourly.cloudCoverHigh(), targetIndex));
        features.put("wind_speed_10m", getAtIndex(hourly.windSpeed10m(), targetIndex));
        features.put("wind_direction_10m", getAtIndex(hourly.windDirection10m(), targetIndex));
        features.put("wind_gusts_10m", getAtIndex(hourly.windGusts10m(), targetIndex));
        features.put("cape", getAtIndex(hourly.cape(), targetIndex));
        features.put("relative_humidity_850hPa", getAtIndex(hourly.relativeHumidity850hPa(), targetIndex));
        features.put("relative_humidity_700hPa", getAtIndex(hourly.relativeHumidity700hPa(), targetIndex));
        features.put("relative_humidity_500hPa", getAtIndex(hourly.relativeHumidity500hPa(), targetIndex));
        features.put("temperature_850hPa", getAtIndex(hourly.temperature850hPa(), targetIndex));
        features.put("temperature_700hPa", getAtIndex(hourly.temperature700hPa(), targetIndex));
        features.put("geopotential_height_500hPa", getAtIndex(hourly.geopotentialHeight500hPa(), targetIndex));
        features.put("precipitation", getAtIndex(hourly.precipitation(), targetIndex));

        Map<String, String> units = Map.ofEntries(
                Map.entry("temperature_2m", "°C"),
                Map.entry("relative_humidity_2m", "%"),
                Map.entry("dew_point_2m", "°C"),
                Map.entry("pressure_msl", "hPa"),
                Map.entry("surface_pressure", "hPa"),
                Map.entry("cloud_cover", "%"),
                Map.entry("cloud_cover_low", "%"),
                Map.entry("cloud_cover_mid", "%"),
                Map.entry("cloud_cover_high", "%"),
                Map.entry("wind_speed_10m", "km/h"),
                Map.entry("wind_direction_10m", "°"),
                Map.entry("wind_gusts_10m", "km/h"),
                Map.entry("cape", "J/kg"),
                Map.entry("relative_humidity_850hPa", "%"),
                Map.entry("relative_humidity_700hPa", "%"),
                Map.entry("relative_humidity_500hPa", "%"),
                Map.entry("temperature_850hPa", "°C"),
                Map.entry("temperature_700hPa", "°C"),
                Map.entry("geopotential_height_500hPa", "m"),
                Map.entry("precipitation", "mm")
        );

        return new GfsFeaturesSnapshot(featureTime, features, units);
    }

    private BigDecimal getAtIndex(List<BigDecimal> list, int index) {
        if (list == null || index < 0 || index >= list.size() || list.get(index) == null) {
            return BigDecimal.ZERO;
        }
        return list.get(index);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenMeteoMarineResponse(
            @JsonProperty("current") MarineCurrent current,
            @JsonProperty("hourly") MarineHourly hourly
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MarineCurrent(
            String time,
            @JsonProperty("sea_level_height_msl") BigDecimal seaLevelHeightMsl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MarineHourly(
            List<String> time,
            @JsonProperty("sea_level_height_msl") List<BigDecimal> seaLevelHeightMsl
    ) {}

    public record GfsFeaturesSnapshot(
            String featureTime,
            Map<String, BigDecimal> features,
            Map<String, String> units
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenMeteoGfsResponse(
            @JsonProperty("hourly_units") Map<String, String> hourlyUnits,
            @JsonProperty("hourly") GfsHourlyData hourly
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GfsHourlyData(
            List<String> time,
            @JsonProperty("temperature_2m") List<BigDecimal> temperature2m,
            @JsonProperty("relative_humidity_2m") List<BigDecimal> relativeHumidity2m,
            @JsonProperty("dew_point_2m") List<BigDecimal> dewPoint2m,
            @JsonProperty("pressure_msl") List<BigDecimal> pressureMsl,
            @JsonProperty("surface_pressure") List<BigDecimal> surfacePressure,
            @JsonProperty("cloud_cover") List<BigDecimal> cloudCover,
            @JsonProperty("cloud_cover_low") List<BigDecimal> cloudCoverLow,
            @JsonProperty("cloud_cover_mid") List<BigDecimal> cloudCoverMid,
            @JsonProperty("cloud_cover_high") List<BigDecimal> cloudCoverHigh,
            @JsonProperty("wind_speed_10m") List<BigDecimal> windSpeed10m,
            @JsonProperty("wind_direction_10m") List<BigDecimal> windDirection10m,
            @JsonProperty("wind_gusts_10m") List<BigDecimal> windGusts10m,
            @JsonProperty("cape") List<BigDecimal> cape,
            @JsonProperty("relative_humidity_850hPa") List<BigDecimal> relativeHumidity850hPa,
            @JsonProperty("relative_humidity_700hPa") List<BigDecimal> relativeHumidity700hPa,
            @JsonProperty("relative_humidity_500hPa") List<BigDecimal> relativeHumidity500hPa,
            @JsonProperty("temperature_850hPa") List<BigDecimal> temperature850hPa,
            @JsonProperty("temperature_700hPa") List<BigDecimal> temperature700hPa,
            @JsonProperty("geopotential_height_500hPa") List<BigDecimal> geopotentialHeight500hPa,
            @JsonProperty("precipitation") List<BigDecimal> precipitation
    ) {}
}
