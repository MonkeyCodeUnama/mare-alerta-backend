package com.marealerta.backend.estabelecimento.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoNegocio {
    MERCADO,
    RESTAURANTE,
    LANCHONETE,
    PADARIA,
    FARMACIA,
    SALAO_BELEZA,
    OFICINA,
    ALIMENTACAO,
    BELEZA,
    VAREJO,
    OUTROS;

    @JsonCreator
    public static TipoNegocio fromString(String value) {
        if (value == null || value.isBlank()) return OUTROS;
        String val = value.trim();
        for (TipoNegocio t : values()) {
            if (t.name().equalsIgnoreCase(val)) {
                return t;
            }
        }
        String normalized = java.text.Normalizer.normalize(val, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replace(" ", "_")
                .replace("/", "_");
        for (TipoNegocio t : values()) {
            if (t.name().equalsIgnoreCase(normalized)) {
                return t;
            }
        }
        if (normalized.contains("ALIMENTA")) return ALIMENTACAO;
        if (normalized.contains("BELEZA")) return BELEZA;
        if (normalized.contains("VAREJO") || normalized.contains("LOJA")) return VAREJO;
        if (normalized.contains("MERCADO")) return MERCADO;
        if (normalized.contains("FARMACIA")) return FARMACIA;
        if (normalized.contains("OFICINA")) return OFICINA;
        return OUTROS;
    }
}
