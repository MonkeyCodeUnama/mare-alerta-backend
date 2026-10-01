package com.marealerta.backend.estabelecimento.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Bairro {
    CAMPINA("Campina"),
    CIDADE_VELHA("Cidade Velha"),
    REDUTO("Reduto"),
    UMARIZAL("Umarizal"),
    NAZARE("Nazaré"),
    BATISTA_CAMPOS("Batista Campos"),
    JURUNAS("Jurunas"),
    CONDOR("Condor"),
    GUAMA("Guamá"),
    CREMACAO("Cremação"),

    MARCO("Marco"),
    PEDREIRA("Pedreira"),
    TELEGRAFO("Telégrafo"),
    SAO_BRAS("São Brás"),
    CANUDOS("Canudos"),
    FATIMA("Fátima"),
    SACRAMENTA("Sacramenta"),
    BARREIRO("Barreiro"),

    MARAMBAIA("Marambaia"),
    CASTANHEIRA("Castanheira"),
    VAL_DE_CAES("Val-de-Cães"),
    SOUSA("Sousa"),
    MANGUEIRAO("Mangueirão"),
    COQUEIRO("Coqueiro"),
    PARQUE_VERDE("Parque Verde"),
    BENGUI("Benguí"),
    TENONE("Tenoné"),
    ICOARACI("Icoaraci"),
    OUTEIRO("Outeiro"),
    MOSQUEIRO("Mosqueiro");

    private final String descricao;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static Bairro fromString(String value) {
        if (value == null || value.isBlank()) return null;
        String val = value.trim();
        for (Bairro b : values()) {
            if (b.name().equalsIgnoreCase(val) || b.descricao.equalsIgnoreCase(val)) {
                return b;
            }
        }
        String normalized = java.text.Normalizer.normalize(val, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");
        for (Bairro b : values()) {
            if (b.name().equalsIgnoreCase(normalized)) {
                return b;
            }
        }
        for (Bairro b : values()) {
            if (normalized.startsWith(b.name()) || b.name().startsWith(normalized)) {
                return b;
            }
        }
        return MARCO;
    }
}
