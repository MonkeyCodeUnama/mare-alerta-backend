package com.marealerta.backend.estabelecimento.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConversorBairro implements AttributeConverter<Bairro, String> {
    @Override
    public String convertToDatabaseColumn(Bairro bairro) {
        return bairro != null ? bairro.name() : null;
    }

    @Override
    public Bairro convertToEntityAttribute(String dbData) {
        return Bairro.fromString(dbData);
    }
}
