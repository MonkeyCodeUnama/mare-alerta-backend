package com.marealerta.backend.alerta.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvolutionApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private EvolutionApiClient client;

    @BeforeEach
    void setUp() {
        client = new EvolutionApiClient(
                restTemplate,
                "http://localhost:8081",
                "secret_key_123",
                "marealerta_belem"
        );
    }

    @Test
    @DisplayName("Deve formatar números brasileiros e de Belém para o padrão E.164")
    void deveFormatarTelefoneCorretamente() {
        assertThat(EvolutionApiClient.formatarTelefoneE164("987654321")).isEqualTo("5591987654321");
        assertThat(EvolutionApiClient.formatarTelefoneE164("87654321")).isEqualTo("559187654321");
        assertThat(EvolutionApiClient.formatarTelefoneE164("(91) 98765-4321")).isEqualTo("5591987654321");
        assertThat(EvolutionApiClient.formatarTelefoneE164("91987654321")).isEqualTo("5591987654321");
        assertThat(EvolutionApiClient.formatarTelefoneE164("+55 (91) 98765-4321")).isEqualTo("5591987654321");
        assertThat(EvolutionApiClient.formatarTelefoneE164(null)).isNull();
    }

    @Test
    @DisplayName("Deve enviar mensagem de texto via POST para a Evolution API")
    @SuppressWarnings("unchecked")
    void deveEnviarTextoComSucesso() {
        EvolutionSendResponse mockResponse = new EvolutionSendResponse(
                new EvolutionSendResponse.Key("MSG-12345", "5591987654321@s.whatsapp.net"),
                "PENDING",
                null
        );

        when(restTemplate.postForObject(
                eq("http://localhost:8081/message/sendText/marealerta_belem"),
                org.mockito.ArgumentMatchers.any(HttpEntity.class),
                eq(EvolutionSendResponse.class)
        )).thenReturn(mockResponse);

        EvolutionSendResponse response = client.enviarTexto("91987654321", "Alerta de teste");

        assertThat(response).isNotNull();
        assertThat(response.getMessageId()).isEqualTo("MSG-12345");

        ArgumentCaptor<HttpEntity<Map<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(
                eq("http://localhost:8081/message/sendText/marealerta_belem"),
                captor.capture(),
                eq(EvolutionSendResponse.class)
        );

        HttpEntity<Map<String, String>> sentEntity = captor.getValue();
        assertThat(sentEntity.getHeaders().getFirst("apikey")).isEqualTo("secret_key_123");
        assertThat(sentEntity.getBody()).containsEntry("number", "5591987654321");
        assertThat(sentEntity.getBody()).containsEntry("text", "Alerta de teste");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o telefone for nulo ou inválido")
    void deveLancarExcecaoParaTelefoneInvalido() {
        assertThatThrownBy(() -> client.enviarTexto("", "Texto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Telefone inválido");

        assertThatThrownBy(() -> client.enviarTexto(null, "Texto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Telefone inválido");
    }
}

