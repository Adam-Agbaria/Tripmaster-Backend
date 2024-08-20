package com.example.app.model;

import com.example.app.util.UserRoleDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = UserRoleDeserializer.class)
public enum UserRole {
    ADMIN,
    SUPERAPP_USER,
    MINIAPP_USER;
}
