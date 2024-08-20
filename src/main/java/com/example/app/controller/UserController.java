package com.example.app.controller;

import com.example.app.exception.CustomBadRequestException;
import com.example.app.exception.CustomConflictException;
import com.example.app.exception.CustomNotFoundException;
import com.example.app.model.NewUserBoundary;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserRole;
import com.example.app.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/superapp/users")
@Tag(name = "Users related API", description = "API for managing users")
public class UserController {

    private final UserService userService;

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new User")
    public ResponseEntity<UserBoundary> createUser(@RequestBody NewUserBoundary user) {
    	
        UserBoundary createdUser = userService.createUser(user);
        return ResponseEntity.status(201).body(createdUser);
    }

    @PutMapping(path = "{superapp}/{email}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update user details")
    public ResponseEntity<Void> updateUser(@PathVariable("superapp") String superapp, @PathVariable("email") String userId, @RequestBody UserBoundary user) {
    	LOGGER.info("Received update request for user: " + userId + " in superapp: " + superapp);
        LOGGER.info("Update user data: " + user.toString());
        userService.updateUser(superapp, userId, user);
        return ResponseEntity.ok().build();
        
    }

    @GetMapping(path = "login/{superapp}/{email}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Login valid user and retrieve user details")
    public ResponseEntity<?> getUserById(@PathVariable("superapp") String superapp, @PathVariable("email") String email) {
        Optional<UserBoundary> userOptional = userService.getUserById(superapp, email);
        if (!userOptional.isPresent()) {
            throw new CustomNotFoundException("User with email " + email + " does not exist.");
        }
        return ResponseEntity.ok(userOptional.get());
    }


}




