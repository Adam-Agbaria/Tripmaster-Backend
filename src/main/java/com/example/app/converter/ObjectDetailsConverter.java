package com.example.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

@Converter
public class ObjectDetailsConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> objectDetails) {
        try {
            return objectMapper.writeValueAsString(objectDetails);
        } catch (IOException e) {
            throw new RuntimeException("Error converting map to JSON string.", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON string to map.", e);
        }
    }
}
