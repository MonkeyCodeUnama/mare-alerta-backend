package com.marealerta.backend.risco.service;

import com.marealerta.backend.risco.dto.PrevisaoMlRequest;
import com.marealerta.backend.risco.dto.PrevisaoMlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class MlApiCliente {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String mlApiUrl;
    private final String mlApiKey;

    public MlApiCliente(ObjectMapper objectMapper, String mlApiUrl) {
        this(objectMapper, mlApiUrl, "");
    }

    @Autowired
    public MlApiCliente(ObjectMapper objectMapper,
                        @Value("${IA_CHUVA_URL:http://weather-model:8000/v1/predict}") String mlApiUrl,
                        @Value("${IA_CHUVA_API_KEY:}") String mlApiKey){
        this.objectMapper = objectMapper;
        this.mlApiUrl = mlApiUrl;
        this.mlApiKey = mlApiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public PrevisaoMlResponse solicitarPrevisao(PrevisaoMlRequest requestData){
        try{
            String jsonPayload = objectMapper.writeValueAsString(requestData);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(mlApiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json");
            if (!mlApiKey.isBlank()) {
                requestBuilder.header("X-API-Key", mlApiKey);
            }
            HttpRequest request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload)).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200){
                throw new RuntimeException("FastAPI retornou erro HTTP " + response.statusCode());
            }
            return  objectMapper.readValue(response.body(), PrevisaoMlResponse.class);
        }catch (Exception e){
            throw new RuntimeException("Falha ao se comunicar com o servico de machine learning" + e.getMessage(), e);
        }
    }




}
