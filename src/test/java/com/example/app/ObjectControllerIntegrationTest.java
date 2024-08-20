package com.example.app;

import com.example.app.converter.ObjectConverter;
import com.example.app.converter.UserConverter;
import com.example.app.model.*;
import com.example.app.repository.EntityObjectRepository;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = TripMaster3Application.class)
@TestPropertySource(locations = "classpath:application.properties")
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ObjectControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "http://localhost:8084";

    @Autowired
    private EntityObjectRepository entityObjectRepository;

    @Autowired
    private UserRepository userRepository;

    private List<EntityObject.ObjectId> createdEntityIds;

    @Autowired
    private ObjectConverter objectConverter;

    @Autowired
    private UserConverter userConverter;

    private List<UserEntity.UserId> userIds;

    @BeforeAll
    public void setup() {
        userIds = new ArrayList<>();
        createdEntityIds = new ArrayList<>();

        // Register users
        registerAndSaveUser("adminTester@example.com", UserRole.ADMIN);
        registerAndSaveUser("superappTester@example.com", UserRole.SUPERAPP_USER);
        registerAndSaveUser("miniappTester@example.com", UserRole.MINIAPP_USER);
    }

    private void registerAndSaveUser(String email, UserRole role) {
        UserBoundary user = createUser("tripMaster", email, role);
        if (user != null && user.getUserId() != null) {
            userIds.add(userConverter.toEntity(user).getUserId());
        }
    }

    @AfterAll
    public void terminator() {
        userIds.forEach(userId -> userRepository.deleteById(userId));
    }

    @BeforeEach
    public void init() {
        createdEntityIds = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        createdEntityIds.forEach(id -> entityObjectRepository.deleteById(id));
    }

    @Test
    public void testCreateObject() {
        BoundaryObject boundaryObject = createBoundaryObject("tripMaster", "superappTester@example.com");

        ResponseEntity<BoundaryObject> response = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObject,
                BoundaryObject.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getObjectId().getId()).isNotEmpty();

        EntityObject newEntityObject = objectConverter.toEntity(response.getBody());
        createdEntityIds.add(newEntityObject.getObjectId());
        
        // Invalid user scenario
        BoundaryObject invalidUserBoundaryObject = createBoundaryObject("tripMaster", "nonExistentUser@example.com");

        ResponseEntity<String> invalidUserResponse = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                invalidUserBoundaryObject,
                String.class);

        assertThat(invalidUserResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // Admin user scenario
        BoundaryObject adminBoundaryObject = createBoundaryObject("tripMaster", "adminTester@example.com");

        ResponseEntity<String> adminUserResponse = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                adminBoundaryObject,
                String.class);

        assertThat(adminUserResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void testGetObjectById() {
        BoundaryObject boundaryObject = createBoundaryObject("tripMaster", "superappTester@example.com");
        BoundaryObject createdObject = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObject,
                BoundaryObject.class).getBody();

        assertThat(createdObject).isNotNull();
        createdEntityIds.add(objectConverter.toEntity(createdObject).getObjectId());

        ResponseEntity<BoundaryObject> response = restTemplate.getForEntity(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/" + createdObject.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                BoundaryObject.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getObjectId().getId()).isEqualTo(createdObject.getObjectId().getId());

        // Miniapp user should get the object if its active
        response = restTemplate.getForEntity(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/" + createdObject.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=miniappTester@example.com",
                BoundaryObject.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getObjectId().getId()).isEqualTo(createdObject.getObjectId().getId());

        // Miniapp user should not get the object if its inactive
        createdObject.setActive(false);
        restTemplate.put(baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/" + createdObject.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                createdObject);

        response = restTemplate.getForEntity(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/" + createdObject.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=miniappTester@example.com",
                BoundaryObject.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Admin user should be forbidden
        response = restTemplate.getForEntity(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/" + createdObject.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=adminTester@example.com",
                BoundaryObject.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        // Try to get an object with a non existent ID
        response = restTemplate.getForEntity(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/nonExistentObjectId" +
                        "?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                BoundaryObject.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testUpdateObject() {
        BoundaryObject boundaryObject = createBoundaryObject("tripMaster", "superappTester@example.com");
        BoundaryObject createdObject = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObject,
                BoundaryObject.class).getBody();

        assertThat(createdObject).isNotNull();
        createdEntityIds.add(objectConverter.toEntity(createdObject).getObjectId());

        createdObject.setAlias("updatedAlias");

        ResponseEntity<Void> updateResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/" + createdObject.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                HttpMethod.PUT,
                new HttpEntity<>(createdObject),
                Void.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<BoundaryObject> response = restTemplate.getForEntity(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/" + createdObject.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                BoundaryObject.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAlias()).isEqualTo("updatedAlias");
        
        // Try to get an object with a non-existent ID
        response = restTemplate.getForEntity(
                baseUrl + "/superapp/objects/" + createdObject.getObjectId().getSuperapp() + "/nonExistentObjectId" +
                        "?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                BoundaryObject.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetAllObjectsPaginated() {

        
        // Create active object
        BoundaryObject boundaryObjectActive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectActive.setActive(true);
        BoundaryObject createdObjectActive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectActive,
                BoundaryObject.class).getBody();

        // Create inactive object
        BoundaryObject boundaryObjectInactive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectInactive.setActive(false);
        BoundaryObject createdObjectInactive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectInactive,
                BoundaryObject.class).getBody();

        assertThat(createdObjectActive).isNotNull();
        assertThat(createdObjectInactive).isNotNull();
        createdEntityIds.add(objectConverter.toEntity(createdObjectActive).getObjectId());
        createdEntityIds.add(objectConverter.toEntity(createdObjectInactive).getObjectId());

        // Superapp user should get all objects
        ResponseEntity<List<BoundaryObject>> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(superappResponse.getBody()).isNotEmpty();

        // Miniapp user should get only active objects
        ResponseEntity<List<BoundaryObject>> miniappActiveResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects?userSuperapp=tripMaster&userEmail=miniappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponse.getBody()).isNotEmpty();
        // Check that all objects are active
        miniappActiveResponse.getBody().forEach(object -> {
            assertThat(object.isActive()).isEqualTo(true);
        });

        // Admin user should be forbidden
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects?userSuperapp=tripMaster&userEmail=adminTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    
    @Test
    public void testSearchObjectsByType() {
        // Initial check for no objects
        ResponseEntity<List<BoundaryObject>> initialResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byType/testType?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initialResponse.getBody()).isEmpty();
        
        BoundaryObject boundaryObjectActive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectActive.setType("testType");
        boundaryObjectActive.setActive(true);
        BoundaryObject createdObjectActive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectActive,
                BoundaryObject.class).getBody();

        BoundaryObject boundaryObjectInactive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectInactive.setType("testType");
        boundaryObjectInactive.setActive(false);
        BoundaryObject createdObjectInactive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectInactive,
                BoundaryObject.class).getBody();

        assertThat(createdObjectActive).isNotNull();
        assertThat(createdObjectInactive).isNotNull();
        createdEntityIds.add(objectConverter.toEntity(createdObjectActive).getObjectId());
        createdEntityIds.add(objectConverter.toEntity(createdObjectInactive).getObjectId());

        // Superapp user should find the object by type
        ResponseEntity<List<BoundaryObject>> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byType/testType?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(superappResponse.getBody()).isNotEmpty();

        // Miniapp user should only get active objects
        ResponseEntity<List<BoundaryObject>> miniappActiveResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byType/testType?userSuperapp=tripMaster&userEmail=miniappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponse.getBody()).isNotEmpty();
        // Check that all objects are active
        miniappActiveResponse.getBody().forEach(object -> {
            assertThat(object.isActive()).isEqualTo(true);
        });

        // Admin user should be forbidden
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byType/testType?userSuperapp=tripMaster&userEmail=adminTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }



    @Test
    public void testSearchObjectsByAlias() {
    	
        // Initial check for no objects
        ResponseEntity<List<BoundaryObject>> initialResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAlias/testAlias?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initialResponse.getBody()).isEmpty();
        
        BoundaryObject boundaryObject = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObject.setAlias("testAlias");
        BoundaryObject createdObject = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObject,
                BoundaryObject.class).getBody();
        
        BoundaryObject createdObjectInactive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObject,
                BoundaryObject.class).getBody();

        assertThat(createdObject).isNotNull();
        assertThat(createdObjectInactive).isNotNull();

        
        createdEntityIds.add(objectConverter.toEntity(createdObject).getObjectId());
        createdEntityIds.add(objectConverter.toEntity(createdObjectInactive).getObjectId());


        // Superapp user should find the object by alias
        ResponseEntity<List<BoundaryObject>> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAlias/testAlias?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(superappResponse.getBody()).isNotEmpty();

        // Miniapp user should find the object by alias if it's active
        boundaryObject.setActive(false);
        restTemplate.put(baseUrl + "/superapp/objects/" + createdObjectInactive.getObjectId().getSuperapp() + "/" + createdObjectInactive.getObjectId().getId() +
                        "?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                boundaryObject);

        ResponseEntity<List<BoundaryObject>> miniappActiveResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAlias/testAlias?userSuperapp=tripMaster&userEmail=miniappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponse.getBody()).isNotEmpty();
        
        // Check that all objects are active
        miniappActiveResponse.getBody().forEach(object -> {
            assertThat(object.isActive()).isEqualTo(true);
        });

        // Admin user should be forbidden
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAlias/testAlias?userSuperapp=tripMaster&userEmail=adminTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    public void testSearchObjectsByAliasPattern() {
    	
        // Initial check for no objects
        ResponseEntity<List<BoundaryObject>> initialResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAliasPattern/pattern?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initialResponse.getBody()).isEmpty();
        
        BoundaryObject boundaryObjectActive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectActive.setAlias("patternAlias");
        boundaryObjectActive.setActive(true);
        BoundaryObject createdObjectActive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectActive,
                BoundaryObject.class).getBody();

        BoundaryObject boundaryObjectInactive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectInactive.setAlias("patternAlias");
        boundaryObjectInactive.setActive(false);
        BoundaryObject createdObjectInactive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectInactive,
                BoundaryObject.class).getBody();

        assertThat(createdObjectActive).isNotNull();
        assertThat(createdObjectInactive).isNotNull();
        createdEntityIds.add(objectConverter.toEntity(createdObjectActive).getObjectId());
        createdEntityIds.add(objectConverter.toEntity(createdObjectInactive).getObjectId());

        // Superapp user should find the object by alias pattern
        ResponseEntity<List<BoundaryObject>> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAliasPattern/pattern?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(superappResponse.getBody()).isNotEmpty();

        // Miniapp user should only get active objects
        ResponseEntity<List<BoundaryObject>> miniappActiveResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAliasPattern/pattern?userSuperapp=tripMaster&userEmail=miniappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponse.getBody()).isNotEmpty();
        // Check that all objects are active
        miniappActiveResponse.getBody().forEach(object -> {
            assertThat(object.isActive()).isEqualTo(true);
        });

        // Admin user should be forbidden
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byAliasPattern/pattern?userSuperapp=tripMaster&userEmail=adminTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void testSearchObjectsByLocation() {
        // Create active object
        BoundaryObject boundaryObjectActive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectActive.setLocation(new BoundaryObject.LocationBoundary(37.8199, -122.4783)); // Golden Gate Bridge
        boundaryObjectActive.setActive(true);
        BoundaryObject createdObjectActive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectActive,
                BoundaryObject.class).getBody();

        // Create inactive object
        BoundaryObject boundaryObjectInactive = createBoundaryObject("tripMaster", "superappTester@example.com");
        boundaryObjectInactive.setLocation(new BoundaryObject.LocationBoundary(37.8199, -122.4783)); // Golden Gate Bridge
        boundaryObjectInactive.setActive(false);
        BoundaryObject createdObjectInactive = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                boundaryObjectInactive,
                BoundaryObject.class).getBody();

        assertThat(createdObjectActive).isNotNull();
        assertThat(createdObjectInactive).isNotNull();
        createdEntityIds.add(objectConverter.toEntity(createdObjectActive).getObjectId());
        createdEntityIds.add(objectConverter.toEntity(createdObjectInactive).getObjectId());

        // Define search point and distance
        double searchLat = 37.7760;
        double searchLng = -122.4348;
        double searchDistanceKm = 6.3; // kilometers
        double searchDistanceMiles = 3.9; // miles 
        double searchDistanceNeutralFind = 0.00098; // degrees, should find
        double searchDistanceNeutralNotFind = 0.00097; // degrees, should not find

        // Superapp user should find the active object within the specified distance (Kilometers)
        ResponseEntity<List<BoundaryObject>> superappResponseKm = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceKm + "?userSuperapp=tripMaster&userEmail=superappTester@example.com&units=KILOMETERS&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(superappResponseKm.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(superappResponseKm.getBody()).isNotEmpty();

        // Miniapp user should only get active objects within the specified distance (Kilometers)
        ResponseEntity<List<BoundaryObject>> miniappActiveResponseKm = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceKm + "?userSuperapp=tripMaster&userEmail=miniappTester@example.com&units=KILOMETERS&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponseKm.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponseKm.getBody()).isNotEmpty();

        // Check that all objects are active
        miniappActiveResponseKm.getBody().forEach(object -> {
            assertThat(object.isActive()).isTrue();
        });

        // Miniapp user should not find objects outside the specified distance (Kilometers)
        double searchDistanceTooFarKm = 6.1; // kilometers
        ResponseEntity<List<BoundaryObject>> miniappActiveResponseTooFarKm = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceTooFarKm + "?userSuperapp=tripMaster&userEmail=miniappTester@example.com&units=KILOMETERS&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponseTooFarKm.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponseTooFarKm.getBody()).isEmpty();

        // Perform the same tests for MILES
        // Superapp user should find the active object within the specified distance (Miles)
        ResponseEntity<List<BoundaryObject>> superappResponseMiles = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceMiles + "?userSuperapp=tripMaster&userEmail=superappTester@example.com&units=MILES&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(superappResponseMiles.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(superappResponseMiles.getBody()).isNotEmpty();

        // Miniapp user should only get active objects within the specified distance (Miles)
        ResponseEntity<List<BoundaryObject>> miniappActiveResponseMiles = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceMiles + "?userSuperapp=tripMaster&userEmail=miniappTester@example.com&units=MILES&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponseMiles.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponseMiles.getBody()).isNotEmpty();

        // Check that all objects are active
        miniappActiveResponseMiles.getBody().forEach(object -> {
            assertThat(object.isActive()).isTrue();
        });

        // Miniapp user should not find objects outside the specified distance (Miles)
        double searchDistanceTooFarMiles = 3.8; // miles (slightly less than 6.1 km)
        ResponseEntity<List<BoundaryObject>> miniappActiveResponseTooFarMiles = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceTooFarMiles + "?userSuperapp=tripMaster&userEmail=miniappTester@example.com&units=MILES&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponseTooFarMiles.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponseTooFarMiles.getBody()).isEmpty();

        // Perform the same tests for NEUTRAL
        // Superapp user should find the active object within the specified distance (Neutral)
        ResponseEntity<List<BoundaryObject>> superappResponseNeutralFind = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceNeutralFind + "?userSuperapp=tripMaster&userEmail=superappTester@example.com&units=NEUTRAL&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(superappResponseNeutralFind.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(superappResponseNeutralFind.getBody()).isNotEmpty();

        // Miniapp user should only get active objects within the specified distance (Neutral)
        ResponseEntity<List<BoundaryObject>> miniappActiveResponseNeutralFind = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceNeutralFind + "?userSuperapp=tripMaster&userEmail=miniappTester@example.com&units=NEUTRAL&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponseNeutralFind.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponseNeutralFind.getBody()).isNotEmpty();

        // Check that all objects are active
        miniappActiveResponseNeutralFind.getBody().forEach(object -> {
            assertThat(object.isActive()).isTrue();
        });

        // Miniapp user should not find objects outside the specified distance (Neutral)
        ResponseEntity<List<BoundaryObject>> miniappActiveResponseNeutralNotFind = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceNeutralNotFind + "?userSuperapp=tripMaster&userEmail=miniappTester@example.com&units=NEUTRAL&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryObject>>() {});
        assertThat(miniappActiveResponseNeutralNotFind.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(miniappActiveResponseNeutralNotFind.getBody()).isEmpty();

        // Admin user should be forbidden
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/objects/search/byLocation/" + searchLat + "/" + searchLng + "/" + searchDistanceKm + "?userSuperapp=tripMaster&userEmail=adminTester@example.com&units=KILOMETERS&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }



    private BoundaryObject createBoundaryObject(String superapp, String email) {
        BoundaryObject boundaryObject = new BoundaryObject();
        BoundaryObject.ObjectIdBoundary objectIdBoundary = new BoundaryObject.ObjectIdBoundary();
        objectIdBoundary.setSuperapp(superapp);
        objectIdBoundary.setId(UUID.randomUUID().toString());
        boundaryObject.setObjectId(objectIdBoundary);

        boundaryObject.setType("testType");
        boundaryObject.setAlias("testAlias");
        boundaryObject.setActive(true);
        boundaryObject.setCreationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        BoundaryObject.CreatedByBoundary.UserIdBoundary userIdBoundary = new BoundaryObject.CreatedByBoundary.UserIdBoundary();
        userIdBoundary.setSuperapp(superapp);
        userIdBoundary.setEmail(email);
        boundaryObject.setCreatedBy(new BoundaryObject.CreatedByBoundary(userIdBoundary));
        boundaryObject.setLocation(new BoundaryObject.LocationBoundary(34.052235, -118.243683));
        boundaryObject.setObjectDetails(new HashMap<>());
        return boundaryObject;
    }

    private UserBoundary createUser(String superapp, String email, UserRole role) {
        NewUserBoundary newUser = new NewUserBoundary();
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setUsername("Test User");
        newUser.setAvatar("avatar.png");

        ResponseEntity<UserBoundary> response = restTemplate.postForEntity(
                baseUrl + "/superapp/users",
                newUser,
                UserBoundary.class);

        return response.getStatusCode() == HttpStatus.CREATED ? response.getBody() : null;
    }
}


