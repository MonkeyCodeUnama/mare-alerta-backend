package com.marealerta.backend.risco.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.marealerta.backend.risco.dto.PrevisaoMlRequest;
import com.marealerta.backend.risco.dto.PrevisaoMlResponse;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MlApiClienteTest {

    private ObjectMapper objectMapper;
    private HttpClient mockHttpClient;
    private MlApiCliente cliente;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockHttpClient = mock(HttpClient.class);
        cliente = new MlApiCliente(objectMapper, "http://127.0.0.1:8000/v1/predict");
        ReflectionTestUtils.setField(cliente, "httpClient", mockHttpClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveSolicitarPrevisaoComSucesso() throws Exception {
        String jsonResposta = """
                {
                    "rain_expected": true,
                    "rain_probability": 0.85,
                    "expected_precipitation_mm": 12.4,
                    "model_version": "rain-hourly-v1",
                    "window_end": "2026-10-12T15:00:00Z"
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResposta);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        PrevisaoMlRequest request = new PrevisaoMlRequest(
                "rain-hourly-v1",
                java.math.BigDecimal.valueOf(-1.4558),
                java.math.BigDecimal.valueOf(-48.5044),
                "2026-10-12T12:00:00Z",
                1,
                java.util.Map.of(),
                java.util.Map.of()
        );

        PrevisaoMlResponse response = cliente.solicitarPrevisao(request);

        assertThat(response).isNotNull();
        assertThat(response.getRainExpected()).isTrue();
        assertThat(response.getRainProbability()).isEqualTo(0.85);
        assertThat(response.getExpectedPrecipitationMm()).isEqualByComparingTo(java.math.BigDecimal.valueOf(12.4));
        assertThat(response.getModelVersion()).isEqualTo("rain-hourly-v1");

        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveLancarExcecaoQuandoFastApiRetornarErroHttp() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        PrevisaoMlRequest request = new PrevisaoMlRequest(
                "rain-hourly-v1",
                java.math.BigDecimal.valueOf(-1.4558),
                java.math.BigDecimal.valueOf(-48.5044),
                "2026-10-12T12:00:00Z",
                1,
                java.util.Map.of(),
                java.util.Map.of()
        );

        assertThatThrownBy(() -> cliente.solicitarPrevisao(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FastAPI retornou erro HTTP 500");
    }
}
