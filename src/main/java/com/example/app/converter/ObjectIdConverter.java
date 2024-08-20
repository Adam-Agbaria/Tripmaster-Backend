package com.example.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


import com.example.app.model.EntityObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@Converter
public class ObjectIdConverter implements AttributeConverter<EntityObject.ObjectId, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(EntityObject.ObjectId objectId) {
        try {
            return objectMapper.writeValueAsString(objectId);
        } catch (IOException e) {
            throw new RuntimeException("Error converting ObjectId to JSON string.", e);
        }
    }

    @Override
    public EntityObject.ObjectId convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, EntityObject.ObjectId.class);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON string to ObjectId.", e);
        }
    }
}
