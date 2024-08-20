package com.example.app.controller;

import com.example.app.exception.CustomBadRequestException;
import com.example.app.exception.CustomConflictException;
import com.example.app.exception.CustomNotAuthorizedOpperation;
import com.example.app.model.BoundaryCommand;
import com.example.app.service.CommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.example.app.service.ObjectService;
import com.example.app.service.UserService;

@RestController
@RequestMapping("/superapp/miniapp/{miniAppName}")
@Tag(name = "MiniApp Command API", description = "API for managing commands")
public class CommandController {

    private final CommandService commandService;
    private static final Logger logger = Logger.getLogger(CommandController.class.getName());

    @Autowired
    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Invoke a command")
    public ResponseEntity<JsonNode> createCommand(
            @PathVariable("miniAppName") String miniAppName,
            @RequestBody BoundaryCommand boundaryCommand) {
        logger.info("Received command for miniApp " + miniAppName + ": " + boundaryCommand);
        
        // Log the received JSON payload
        ObjectMapper mapper = new ObjectMapper(); 
        try {
            String receivedJson = mapper.writeValueAsString(boundaryCommand);
            logger.info("Received JSON: " + receivedJson);

            BoundaryCommand createdCommand = commandService.createCommand(boundaryCommand, miniAppName);
            JsonNode commandJson = mapper.convertValue(createdCommand, JsonNode.class);
            ArrayNode responseArray = mapper.createArrayNode().add(commandJson);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseArray);
        } catch (CustomBadRequestException | CustomNotAuthorizedOpperation e) {
            logger.severe("Error creating command: " + e.getMessage());
            throw e;  
        } catch (Exception e) {
            logger.severe("Error creating command: " + e.getMessage());
            throw new CustomBadRequestException("Failed to create command: " + e.getMessage());
        }
    }
}