package com.example.app.controller;

import com.example.app.model.BoundaryCommand;
import com.example.app.model.UserBoundary;
import com.example.app.service.AdminService;
import com.example.app.updatedService.UpdatedAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superapp/admin")
@Tag(name = "Updated Admin Api", description = "API for managing commands")
public class AdminController {

    private final UpdatedAdminService adminService;

    @Autowired
    public AdminController(UpdatedAdminService adminService) {
        this.adminService = adminService;
    }


    @DeleteMapping(path = "/users",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated Delete all users")
    public ResponseEntity<Void> deleteAllUsers(
    		@RequestParam(name = "userSuperapp") String userSuperapp,
    		@RequestParam(name = "userEmail") String userEmail) {
        adminService.deleteAllUsers(userSuperapp, userEmail);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/objects",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated Delete all objects")
    public ResponseEntity<Void> deleteAllObjects(
    		@RequestParam(name = "userSuperapp") String userSuperapp,
    		@RequestParam(name = "userEmail") String userEmail) {
        adminService.deleteAllObjects(userSuperapp, userEmail);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/miniapp",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated Delete all commands")
    public ResponseEntity<Void> deleteAllCommands(
    		@RequestParam(name = "userSuperapp") String userSuperapp,
    		@RequestParam(name = "userEmail") String userEmail) {
        adminService.deleteAllCommands(userSuperapp, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/users",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated Export all users using pagination")
    public ResponseEntity<List<UserBoundary>> getAllUsers(
    		@RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
        List<UserBoundary> users = adminService.getAllUsers(userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/miniapp",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated Export all MiniApp commands using pagination")
    public ResponseEntity<List<BoundaryCommand>> exportAllCommandsHistory(
    		@RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
        List<BoundaryCommand> commands = adminService.exportAllCommandsHistory(userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(commands);
    }

    @GetMapping(path = "/miniapp/{miniAppName}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated Export specific MiniApp commands using pagination")
    public ResponseEntity<List<BoundaryCommand>> exportCommandsHistoryByMiniApp(
            @PathVariable("miniAppName") String miniAppName,
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
        List<BoundaryCommand> commands = adminService.exportCommandsHistoryByMiniApp(miniAppName, userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(commands);
    }
    

    
}
