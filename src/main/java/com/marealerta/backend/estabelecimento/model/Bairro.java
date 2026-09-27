package com.marealerta.backend.estabelecimento.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Bairro {
    // Zona Central / Comercial / Orla (Área de Risco de Maré)
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

    // Zona Sul / Leste
    MARCO("Marco"),
    PEDREIRA("Pedreira"),
    TELEGRAFO("Telégrafo"),
    SAO_BRAS("São Brás"),
    CANUDOS("Canudos"),
    FATIMA("Fátima"),
    SACRAMENTA("Sacramenta"),
    BARREIRO("Barreiro"),

    // Expansão / Entorno e Distritos
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
}
