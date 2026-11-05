package com.marealerta.backend.estabelecimento.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Bairro {
    CAMPINA("Campina", -1.4531, -48.4998, 2.80, "Estrada Nova", "CRITICO"),
    CIDADE_VELHA("Cidade Velha", -1.4558, -48.5044, 2.70, "Estrada Nova", "CRITICO"),
    REDUTO("Reduto", -1.4510, -48.4940, 2.90, "Estrada Nova", "ALTO"),
    UMARIZAL("Umarizal", -1.4430, -48.4870, 3.40, "Estrada Nova", "MEDIO"),
    NAZARE("Nazaré", -1.4520, -48.4820, 8.50, "Tucunduba", "BAIXO"),
    BATISTA_CAMPOS("Batista Campos", -1.4600, -48.4900, 6.00, "Estrada Nova", "BAIXO"),
    JURUNAS("Jurunas", -1.4700, -48.4950, 2.75, "Estrada Nova", "CRITICO"),
    CONDOR("Condor", -1.4680, -48.4750, 2.85, "Estrada Nova", "CRITICO"),
    GUAMA("Guamá", -1.4650, -48.4600, 2.90, "Tucunduba", "CRITICO"),
    CREMACAO("Cremação", -1.4620, -48.4830, 3.50, "Estrada Nova", "MEDIO"),

    MARCO("Marco", -1.4330, -48.4650, 12.00, "Una", "BAIXO"),
    PEDREIRA("Pedreira", -1.4300, -48.4750, 4.50, "Una", "MEDIO"),
    TELEGRAFO("Telégrafo", -1.4350, -48.4900, 3.20, "Una", "ALTO"),
    SAO_BRAS("São Brás", -1.4500, -48.4680, 9.00, "Tucunduba", "BAIXO"),
    CANUDOS("Canudos", -1.4530, -48.4570, 5.50, "Tucunduba", "MEDIO"),
    FATIMA("Fátima", -1.4450, -48.4770, 6.00, "Una", "BAIXO"),
    SACRAMENTA("Sacramenta", -1.4250, -48.4850, 3.30, "Una", "ALTO"),
    BARREIRO("Barreiro", -1.4150, -48.4900, 3.10, "Una", "ALTO"),

    MARAMBAIA("Marambaia", -1.3950, -48.4450, 8.00, "Una", "BAIXO"),
    CASTANHEIRA("Castanheira", -1.3900, -48.4350, 9.00, "Murucutu", "BAIXO"),
    VAL_DE_CAES("Val-de-Cães", -1.3980, -48.4800, 3.50, "Una", "MEDIO"),
    SOUSA("Sousa", -1.4120, -48.4420, 7.00, "Murucutu", "BAIXO"),
    MANGUEIRAO("Mangueirão", -1.3750, -48.4400, 8.00, "Una", "BAIXO"),
    COQUEIRO("Coqueiro", -1.3450, -48.4300, 7.50, "Maguari", "BAIXO"),
    PARQUE_VERDE("Parque Verde", -1.3650, -48.4550, 8.00, "Una", "BAIXO"),
    BENGUI("Benguí", -1.3700, -48.4680, 6.00, "Una", "MEDIO"),
    TENONE("Tenoné", -1.3150, -48.4600, 6.50, "Maguari", "BAIXO"),
    ICOARACI("Icoaraci", -1.2980, -48.4800, 3.10, "Maguari", "ALTO"),
    OUTEIRO("Outeiro", -1.2400, -48.4450, 3.30, "Ilhas", "MEDIO"),
    MOSQUEIRO("Mosqueiro", -1.1350, -48.4100, 3.40, "Ilhas", "MEDIO");

    private final String descricao;
    private final Double latitude;
    private final Double longitude;
    private final Double cotaTransbordamentoMetros;
    private final String bacia;
    private final String nivelVulnerabilidade;

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
        if (normalized.contains("CENTRO") || normalized.contains("COMERCIO")) {
            return CAMPINA;
        }
        for (Bairro b : values()) {
            if (normalized.startsWith(b.name()) || b.name().startsWith(normalized)) {
                return b;
            }
        }
        return MARCO;
    }
}
