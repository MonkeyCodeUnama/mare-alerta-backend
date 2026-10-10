package com.marealerta.backend.alerta.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvolutionSendResponse(
        @JsonProperty("key") Key key,
        @JsonProperty("status") String status,
        @JsonProperty("id") String directId
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Key(
            @JsonProperty("id") String id,
            @JsonProperty("remoteJid") String remoteJid
    ) {}

    public String getMessageId() {
        if (key != null && key.id() != null && !key.id().isBlank()) {
            return key.id();
        }
        return directId;
    }
}

