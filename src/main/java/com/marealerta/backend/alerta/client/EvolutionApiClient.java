package com.marealerta.backend.alerta.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class EvolutionApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String instancia;

    public EvolutionApiClient(
            RestTemplate restTemplate,
            @Value("${whatsapp.evolution.url:http://localhost:8081}") String baseUrl,
            @Value("${whatsapp.evolution.api-key:marealerta_secret_key_2026}") String apiKey,
            @Value("${whatsapp.evolution.instancia:marealerta_belem}") String instancia
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.instancia = instancia;
    }

    public EvolutionSendResponse enviarTexto(String telefone, String mensagem) {
        String telefoneFormatado = formatarTelefoneE164(telefone);
        if (telefoneFormatado == null || telefoneFormatado.isBlank()) {
            throw new IllegalArgumentException("Telefone inválido para envio de WhatsApp");
        }

        String url = baseUrl + "/message/sendText/" + instancia;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);

        Map<String, String> body = Map.of(
                "number", telefoneFormatado,
                "text", mensagem
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, request, EvolutionSendResponse.class);
    }

    public static String formatarTelefoneE164(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() == 8 || digits.length() == 9) {
            return "5591" + digits;
        }
        if (digits.length() == 10 || digits.length() == 11) {
            return "55" + digits;
        }
        return digits;
    }
}

