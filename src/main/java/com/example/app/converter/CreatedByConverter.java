package com.example.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.example.app.model.EntityObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@Converter
public class CreatedByConverter implements AttributeConverter<EntityObject.CreatedBy, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(EntityObject.CreatedBy createdBy) {
        try {
            return objectMapper.writeValueAsString(createdBy);
        } catch (IOException e) {
            throw new RuntimeException("Error converting CreatedBy to JSON string.", e);
        }
    }

    @Override
    public EntityObject.CreatedBy convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, EntityObject.CreatedBy.class);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON string to CreatedBy.", e);
        }
    }
}
