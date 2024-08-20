package com.example.app.controller;

import com.example.app.model.BoundaryObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import com.example.app.updatedService.UpdatedObjectService;
import com.example.app.serviceImpl.ObjectServiceImpl;
import com.example.app.exception.*;
import com.example.app.exception.UnsupportedOperationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/superapp/objects")
@Tag(name = "Updated Super App Objects API", description = "API for managing objects")
public class ObjectController {

    private final UpdatedObjectService objectService;

    @Value("${spring.application.name}")
    private String appName;

    private static final Logger LOGGER = Logger.getLogger(ObjectController.class.getName());

    @Autowired
    public ObjectController(UpdatedObjectService objectService) {
        this.objectService = objectService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new object")
    public ResponseEntity<?> createObject(@RequestBody BoundaryObject boundaryObject) {
            BoundaryObject createdObject = objectService.createObject(boundaryObject);
            LOGGER.info("Created object: " + createdObject);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdObject);
       
    }

    
    @GetMapping(path = "/{superapp}/{id}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated API for get Get specific object")
    public ResponseEntity<?> getObjectById(
    		@PathVariable("superapp") String superapp,
            @PathVariable("id") String id,
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail) {
        BoundaryObject object = objectService.getObject(superapp, id, userSuperapp, userEmail);
        LOGGER.info("Created object: " + object);


        return ResponseEntity.ok(object);
    }


    
    @PutMapping(path = "/{superapp}/{id}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated API for object Update")
    public ResponseEntity<String> updateObject(
            @PathVariable("superapp") String superapp,
            @PathVariable("id") String id,
            @RequestBody BoundaryObject boundaryObject,
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail) {
    	
            objectService.updateObject(superapp, id, userSuperapp, userEmail, boundaryObject);
            return ResponseEntity.ok().build();

    }
    

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updated API for get all objects using pagination")
    public ResponseEntity<List<BoundaryObject>> getAllObjectsPaginated(
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
    	
        List<BoundaryObject> objects = objectService.getAllObjects(userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(objects);
    }
    
    @GetMapping(path = "/search/byType/{type}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "New search objects by type with pagination support")
    public ResponseEntity<List<BoundaryObject>> searchObjectsByType(
            @PathVariable("type") String type,
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page){
        List<BoundaryObject> objects = objectService.searchObjectsByType(type, userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(objects);
    }

    @GetMapping(path = "/search/byAlias/{alias}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "New search objects by exact alias with pagination support")
    public ResponseEntity<List<BoundaryObject>> searchObjectsByAlias(
            @PathVariable("alias") String alias,
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
        List<BoundaryObject> objects = objectService.searchObjectsByAlias(alias, userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(objects);
    }

    @GetMapping(path = "/search/byAliasPattern/{pattern}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "New search objects by alias pattern with pagination support")
    public ResponseEntity<List<BoundaryObject>> searchObjectsByAliasPattern(
            @PathVariable("pattern") String pattern,
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
        List<BoundaryObject> objects = objectService.searchObjectsByAliasPattern(pattern, userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(objects);
    }

    @GetMapping(path = "/search/byLocation/{lat}/{lng}/{distance}",
    		produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "New search objects by location with pagination support")
    public ResponseEntity<List<BoundaryObject>> searchObjectsByLocation(
            @PathVariable("lat") double lat,
            @PathVariable("lng") double lng,
            @PathVariable("distance") double distance,
            @RequestParam(name = "units",defaultValue = "Neutral") String units,
            @RequestParam(name = "userSuperapp") String userSuperapp,
            @RequestParam(name = "userEmail") String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
        List<BoundaryObject> objects = objectService.searchObjectsByLocation(lat, lng, distance, units, userSuperapp, userEmail, size, page);
        return ResponseEntity.ok(objects);
    }
    
    


    
}
