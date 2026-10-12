package com.marealerta.backend.telemetria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class OpenMeteoClientTest {

    @Mock
    private RestTemplate restTemplate;

    private OpenMeteoClient client;

    @BeforeEach
    void setUp() {
        client = new OpenMeteoClient(restTemplate);
    }

    @Test
    void deveBuscarDadosAtuaisEPrevisaoComSucesso() {
        OpenMeteoClient.CurrentUnits current = new OpenMeteoClient.CurrentUnits(
                "2026-10-12T12:00",
                BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(28.4),
                BigDecimal.valueOf(14.2),
                BigDecimal.valueOf(80.0)
        );

        OpenMeteoClient.HourlyData hourly = new OpenMeteoClient.HourlyData(
                List.of("2026-10-12T13:00", "2026-10-12T14:00"),
                List.of(BigDecimal.valueOf(0.0), BigDecimal.valueOf(5.2)),
                List.of(BigDecimal.valueOf(29.0), BigDecimal.valueOf(27.5)),
                List.of(BigDecimal.valueOf(10.0), BigDecimal.valueOf(18.0)),
                List.of(BigDecimal.valueOf(70.0), BigDecimal.valueOf(85.0))
        );

        OpenMeteoClient.OpenMeteoResponse mockResponse = new OpenMeteoClient.OpenMeteoResponse(current, hourly);

        when(restTemplate.getForObject(contains("latitude=-1.4558"), eq(OpenMeteoClient.OpenMeteoResponse.class)))
                .thenReturn(mockResponse);

        OpenMeteoClient.OpenMeteoResponse response = client.buscarDadosAtuaisEPrevisao();

        assertThat(response).isNotNull();
        assertThat(response.current()).isNotNull();
        assertThat(response.current().temperatura()).isEqualByComparingTo(BigDecimal.valueOf(28.4));
        assertThat(response.current().precipitacao()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        assertThat(response.hourly().time()).hasSize(2);

        verify(restTemplate).getForObject(contains("timezone=America/Belem"), eq(OpenMeteoClient.OpenMeteoResponse.class));
    }
}

