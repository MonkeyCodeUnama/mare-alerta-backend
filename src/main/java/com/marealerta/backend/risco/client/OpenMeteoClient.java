package com.marealerta.backend.risco.client;

import com.marealerta.backend.risco.dto.OpenMeteoApiResponse;
import com.marealerta.backend.risco.dto.PrevisaoMlRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class OpenMeteoClient {

    private final RestClient restClient;

    public OpenMeteoClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }

    public PrevisaoMlRequest buscarDadosAtuais(double latitude, double longitude, String localidade) {
        OpenMeteoApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current_weather", true)
                        .build())
                .retrieve()
                .body(OpenMeteoApiResponse.class);

        if (response == null || response.currentWeather() == null) {
            throw new RuntimeException("Falha ao obter dados da Open-Meteo");
        }

        OpenMeteoApiResponse.CurrentWeather weather = response.currentWeather();

        return PrevisaoMlRequest.builder()
                .localidade(localidade)
                .instanteReferencia(LocalDateTime.now())
                .horizonteHoras(1)
                .temperatura2m(BigDecimal.valueOf(weather.temperature()))
                .umidadeRelativa2m(BigDecimal.valueOf(80.0)) // Valor padrão/simulado se não mapeado no endpoint
                .pressaoSuperficie(BigDecimal.valueOf(1013.25))
                .coberturaNuvens(BigDecimal.ZERO)
                .velocidadeVento10m(BigDecimal.valueOf(weather.windspeed()))
                .direcaoVento10m(BigDecimal.valueOf(weather.winddirection()))
                .precipitacaoObservada1h(BigDecimal.ZERO)
                .cape(BigDecimal.ZERO)
                .build();
    }
}