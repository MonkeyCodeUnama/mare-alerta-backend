package com.marealerta.backend.risco.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {

    private static final List<String> MODEL_FEATURES = List.of(
            "temperature_2m", "relative_humidity_2m", "dew_point_2m", "pressure_msl",
            "surface_pressure", "cloud_cover", "cloud_cover_low", "cloud_cover_mid",
            "cloud_cover_high", "wind_speed_10m", "wind_direction_10m", "wind_gusts_10m",
            "cape", "relative_humidity_850hPa", "relative_humidity_700hPa",
            "relative_humidity_500hPa", "temperature_850hPa", "temperature_700hPa",
            "geopotential_height_500hPa", "precipitation");

    private final RestClient restClient;

    public OpenMeteoClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }

    public DadosMeteo buscarDadosAtuais(double latitude, double longitude, String localidade) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("hourly", MODEL_FEATURES.stream().collect(Collectors.joining(",")))
                        .queryParam("forecast_days", 2)
                        .queryParam("timezone", "UTC")
                        .queryParam("wind_speed_unit", "kmh")
                        .queryParam("precipitation_unit", "mm")
                        .queryParam("temperature_unit", "celsius")
                        .queryParam("models", "gfs_global")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.containsKey("error")) {
            throw new IllegalStateException("Resposta inválida da Open-Meteo: " + response);
        }

        Map<String, Object> hourly = asMap(response.get("hourly"), "hourly");
        Map<String, Object> hourlyUnits = asMap(response.get("hourly_units"), "hourly_units");
        List<?> times = asList(hourly.get("time"), "hourly.time");
        HourSample sample = findCurrentHour(times);
        OffsetDateTime featureTime = sample.featureTime();
        int index = sample.index();

        Map<String, BigDecimal> features = new LinkedHashMap<>();
        Map<String, String> units = new LinkedHashMap<>();
        for (String name : MODEL_FEATURES) {
            List<?> values = asList(hourly.get(name), "hourly." + name);
            if (values.size() != times.size()) {
                throw new IllegalStateException("Série Open-Meteo desalinhada para " + name);
            }
            Object value = values.get(index);
            if (value != null && !(value instanceof Number)) {
                throw new IllegalStateException("Valor inválido da Open-Meteo para " + name);
            }
            features.put(name, value == null ? null : new BigDecimal(value.toString()));

            Object unit = hourlyUnits.get(name);
            if (!(unit instanceof String unitText)) {
                throw new IllegalStateException("Unidade ausente na Open-Meteo para " + name);
            }
            units.put(name, unitText);
        }

        return new DadosMeteo(featureTime, features, units);
    }

    private HourSample findCurrentHour(List<?> times) {
        LocalDateTime currentHour = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
        for (int index = 0; index < times.size(); index++) {
            Object timeValue = times.get(index);
            if (timeValue instanceof String timeText
                    && LocalDateTime.parse(timeText, DateTimeFormatter.ISO_LOCAL_DATE_TIME).equals(currentHour)) {
                return new HourSample(currentHour.atOffset(ZoneOffset.UTC), index);
            }
        }
        throw new IllegalStateException("Hora UTC atual não está disponível na série da Open-Meteo");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value, String field) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalStateException("Campo ausente ou inválido na Open-Meteo: " + field);
        }
        return (Map<String, Object>) value;
    }

    private List<?> asList(Object value, String field) {
        if (!(value instanceof List<?> values)) {
            throw new IllegalStateException("Campo ausente ou inválido na Open-Meteo: " + field);
        }
        return values;
    }

    public record DadosMeteo(OffsetDateTime featureTime, Map<String, BigDecimal> features, Map<String, String> units) {}

    private record HourSample(OffsetDateTime featureTime, int index) {}
}
