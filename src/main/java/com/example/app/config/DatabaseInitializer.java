package com.example.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeDatabase() {
        createUsersTable();
        createObjectsTable();
        createCommandsTable();
    }

    private void createUsersTable() {
        logger.info("Checking if the 'USERS' table needs to be created...");
        String sqlUsers = """
            CREATE TABLE IF NOT EXISTS USERS (
                superapp VARCHAR(255),
                email VARCHAR(255),
                role VARCHAR(255),
                username VARCHAR(255),
                avatar VARCHAR(255),
                PRIMARY KEY (superapp, email)
            );
            """;
        jdbcTemplate.execute(sqlUsers);
        logger.info("Checked/created 'USERS' table.");
    }

    private void createObjectsTable() {
        logger.info("Checking if the 'OBJECTS' table needs to be created...");
        String sqlObjects = """
            CREATE TABLE IF NOT EXISTS OBJECTS (
                superapp VARCHAR(255),
                id VARCHAR(255),
                type VARCHAR(255),
                alias VARCHAR(255),
                location_id BIGINT,
                active BOOL,
                creationTimestamp TIMESTAMP,
                createdBy TEXT,
                objectDetails TEXT,
                PRIMARY KEY (superapp, id),
                FOREIGN KEY (location_id) REFERENCES Location(id)
            );
            """;
        jdbcTemplate.execute(sqlObjects);
        logger.info("Checked/created 'OBJECTS' table.");
    }

    private void createCommandsTable() {
        logger.info("Checking if the 'COMMANDS' table needs to be created...");
        String sqlCommands = """
            CREATE TABLE IF NOT EXISTS COMMANDS (
                id VARCHAR(255),
                superapp VARCHAR(255),
                miniapp VARCHAR(255),
                command VARCHAR(255),
                targetObjectSuperapp VARCHAR(255),
                targetObjectId VARCHAR(255),
                invocationTimestamp TIMESTAMP,
                invokedBySuperapp VARCHAR(255),
                invokedByEmail VARCHAR(255),
                commandAttributes VARCHAR(255),
                PRIMARY KEY (id)
            );
            """;
        jdbcTemplate.execute(sqlCommands);
        logger.info("Checked/created 'COMMANDS' table.");
    }


}
