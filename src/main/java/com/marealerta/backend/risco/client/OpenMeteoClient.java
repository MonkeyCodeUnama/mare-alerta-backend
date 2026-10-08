package com.marealerta.backend.risco.client;

import com.marealerta.backend.risco.dto.OpenMeteoApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {

    private final RestClient restClient;

    public OpenMeteoClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }

    public DadosMeteo buscarDadosAtuais(double latitude, double longitude, String localidade) {
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

        return new DadosMeteo(
                weather.temperature(),
                80.0,     // umidade padrão/simulada
                1013.25,  // pressão padrão
                0.0,      // nuvens
                weather.windspeed(),
                weather.winddirection(),
                0.0,      // precipitação
                0.0       // cape
        );
    }

    public record DadosMeteo(
            double temperatura,
            double umidade,
            double pressao,
            double nuvens,
            double ventoVelocidade,
            double ventoDirecao,
            double precipitacao,
            double cape
    ) {}
}