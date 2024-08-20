package com.example.app.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply=true)
public class GeneralJsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GeneralJsonConverter.class);


    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) return null;
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.info("Converting to database column: " + json);
            return json;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting map to JSON string", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Map<String, Object> map = objectMapper.readValue(dbData, Map.class);
            logger.info("Converting to entity attribute: " + map);
            return map;
        } catch (IOException e) {
        throw new IllegalArgumentException("Error converting JSON string to map", e);
        }
    }

}
