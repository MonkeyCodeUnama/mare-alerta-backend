package com.marealerta.backend.alerta.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

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

    public Map<String, Object> consultarEstadoConexao() {
        try {
            return chamar("/instance/connectionState/" + instancia, "GET", null);
        } catch (HttpClientErrorException.NotFound e) {
            return Map.of("state", "NOT_CREATED");
        }
    }
    public Object consultarInstancia() { return chamarLista("/instance/fetchInstances?instanceName=" + instancia); }
    public Map<String, Object> obterQrCode() { return chamar("/instance/connect/" + instancia, "GET", null); }
    public Map<String, Object> conectar() {
        try {
            return chamar("/instance/connect/" + instancia, "GET", null);
        } catch (HttpClientErrorException.NotFound e) {
            return criarInstancia();
        }
    }

    private Map<String, Object> criarInstancia() {
        return chamar("/instance/create", "POST", Map.of("instanceName", instancia, "integration", "WHATSAPP-BAILEYS", "qrcode", true));
    }
    public Map<String, Object> desconectar() { return chamar("/instance/logout/" + instancia, "DELETE", null); }
    public Map<String, Object> reiniciarInstancia() { desconectar(); return conectar(); }

    private Object chamarLista(String caminho) {
        HttpHeaders headers = new HttpHeaders(); headers.set("apikey", apiKey);
        return restTemplate.exchange(baseUrl + caminho, org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), Object.class).getBody();
    }

    private Map<String, Object> chamar(String caminho, String metodo, Object body) {
        HttpHeaders headers = new HttpHeaders(); headers.set("apikey", apiKey); headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(body, headers);
        return switch (metodo) {
            case "POST" -> restTemplate.exchange(baseUrl + caminho, org.springframework.http.HttpMethod.POST, request, Map.class).getBody();
            case "DELETE" -> restTemplate.exchange(baseUrl + caminho, org.springframework.http.HttpMethod.DELETE, request, Map.class).getBody();
            default -> restTemplate.exchange(baseUrl + caminho, org.springframework.http.HttpMethod.GET, request, Map.class).getBody();
        };
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

