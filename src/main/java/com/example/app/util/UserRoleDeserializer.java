package com.example.app.util;

import com.example.app.exception.CustomBadRequestException;
import com.example.app.model.UserRole;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class UserRoleDeserializer extends JsonDeserializer<UserRole> {

    @Override
    public UserRole deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getText().toUpperCase();

        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new CustomBadRequestException("Invalid user role: " + value);
        }
    }
}
